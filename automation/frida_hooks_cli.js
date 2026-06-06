/**
 * Frida Dynamic Call Graph Hook — Production Version.
 *
 * Hooks methods in de.danoeh.antennapod.* classes at runtime.
 * Uses Exception.getStackTrace() for caller detection (avoids
 * Thread.sleep crash in Frida 17.10.1 on Android 12).
 *
 * Safety: limits hooks to first 80 classes to avoid Frida
 * native crash on problematic enum/interface/synthetic classes
 * encountered during enumeration.
 *
 * Usage:
 *     frida -U -f de.danoeh.antennapod.debug --runtime=v8 -l this.js -q -t 30 -o out.txt
 *
 * Author: Tianyu Yao
 */

'use strict';

var TARGET = 'de.danoeh.antennapod';
var MAX_PER_CLASS = 15;
var MAX_CLASSES = 50;
var STACK_DEPTH = 30;
var SCAN_DELAY_MS = 4000;
var RESCAN_MS = 6000;
var FLUSH_MS = 2000;

var Exception = null;
var edgeBuf = [], totalEdges = 0, totalHooks = 0;
var classCount = 0, hookedSet = {};

function skipMethod(n) {
    if (n === 'toString' || n === 'hashCode' || n === 'equals' || n === 'getClass' ||
        n === 'notify' || n === 'notifyAll' || n === 'wait' || n === 'finalize' ||
        n === 'clone' || n === 'values' || n === 'valueOf' || n === '$values') return true;
    if (n.indexOf('access$') === 0 || n.indexOf('lambda$') === 0 ||
        n.indexOf('$r8$') === 0 || n.indexOf('$default') === 0) return true;
    if (n.indexOf('$$') >= 0) return true;
    return false;
}

function skipClass(name) {
    if (name.indexOf('BuildConfig') >= 0 || name.indexOf('Manifest$') >= 0) return true;
    if (name.indexOf('.R$') >= 0 || name.slice(-2) === '.R') return true;
    if (name.indexOf('$$ExternalSynthetic') >= 0) return true;
    var i = name.lastIndexOf('$');
    if (i >= 0 && /^\d+$/.test(name.slice(i + 1))) return true;
    return false;
}

function findCaller(cls, mn) {
    try {
        var stack = Exception.$new().getStackTrace();
        for (var i = 1; i < Math.min(stack.length, STACK_DEPTH); i++) {
            var fc = stack[i].getClassName();
            if (fc.indexOf(TARGET) === 0) {
                var fm = stack[i].getMethodName();
                if (fc === cls && fm === mn) continue;
                return fc + '.' + fm;
            }
        }
    } catch(e) {}
    return '';
}

function flushBuf() {
    if (!edgeBuf.length) return;
    console.log(JSON.stringify({type:'edges',payload:edgeBuf.slice()}));
    edgeBuf = [];
}

function hookOne(cls, mn, overloads) {
    for (var oi = 0; oi < Math.min(overloads.length, 2); oi++) {
        (function(ov, cn, mName) {
            var orig = ov.implementation;
            ov.implementation = function() {
                var callee = cn + '.' + mName;
                var caller = findCaller(cn, mName);
                if (caller) { edgeBuf.push({caller:caller,callee:callee}); totalEdges++; }
                if (edgeBuf.length >= 50) flushBuf();
                // Call original — use ov.call() which Frida routes to native impl
                try { return ov.call(this, arguments); } catch(e) { return undefined; }
            };
            totalHooks++;
        })(overloads[oi], cls, mn);
    }
}

function hookClass(className) {
    if (classCount >= MAX_CLASSES || hookedSet[className] || skipClass(className)) return 0;
    var clazz, co, methods;
    try { clazz = Java.use(className); } catch(e) { return 0; }
    try { co = clazz.class; } catch(e) { return 0; }
    try { if (co.isEnum() || co.isInterface()) return 0; } catch(e) {}
    try { methods = co.getDeclaredMethods(); } catch(e) { return 0; }
    var h = 0;
    for (var i = 0; i < Math.min(methods.length, MAX_PER_CLASS); i++) {
        try {
            var mn = methods[i].getName();
            if (skipMethod(mn)) continue;
            if (mn === '<clinit>' || mn === '<init>' || mn.indexOf('-$$') === 0) continue;
            var ovs = clazz[mn].overloads;
            if (ovs && ovs.length) { hookOne(className, mn, ovs); h++; }
        } catch(e) {}
    }
    if (h) { hookedSet[className] = true; classCount++; }
    return h;
}

function scan() {
    Java.perform(function() {
        if (!Exception) { try { Exception = Java.use('java.lang.Exception'); } catch(e) { return; } }
        var nh = 0, attempted = 0;
        Java.enumerateLoadedClasses({
            onMatch: function(name) {
                if (attempted >= 30) return; // safety: stop early
                if (name.indexOf(TARGET) !== 0) return;
                attempted++;
                try { var h = hookClass(name); if (h) nh += h; } catch(e) {}
            },
            onComplete: function() {
                flushBuf();
                console.log(JSON.stringify({type:'status',classes:classCount,hooks:totalHooks,newHooks:nh,edges:totalEdges}));
            }
        });
    });
}

Java.perform(function() {
    console.log(JSON.stringify({type:'init'}));
    try { Exception = Java.use('java.lang.Exception'); } catch(e) {}
    setTimeout(scan, SCAN_DELAY_MS);
    setInterval(scan, RESCAN_MS);
    setInterval(flushBuf, FLUSH_MS);
});
setInterval(function(){}, 60000);
