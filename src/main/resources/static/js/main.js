import { connect, registerSubscription } from "./websocket.js";

export function registerMain() {
    registerSubscription((client) => {
        client.subscribe("/user/queue/errors", (msg) => {
            showAlert('danger', msg.body);
        })
    })
}

const alertPlaceholder = document.getElementById('alert');


export function callbackClosure(i, callback) {
    return function () {
        return callback(i);
    }
}

export function showAlert(type, message, duration) {
    const wrapper = document.createElement('div')
    wrapper.innerHTML = [
        `<div class="alert alert-${type} alert-dismissible" role="alert">`,
        `   ${message}`,
        '</div>'
    ].join('')

    alertPlaceholder.append(wrapper)

    setTimeout(() => wrapper.remove(), duration || 5000);
}

export function getTextColorForBackground(l) {
    // Wenn Lichtstärke unter 50%, Text weiß, sonst dunkel
    return l < 50 ? '#fff' : '#2d2d2d';
}

export function deriveButtonColors(categoryHex) {
    const hsl = hexToHSL(categoryHex);

    // Dunklerer Ton für Button normal
    let normalL = Math.min(100, hsl.l + 15);
    // Hellerer Ton für Hover
    let hoverL = Math.min(100, hsl.l + 30);

    return {
        buttonNormal: hslToHex(hsl.h, hsl.s, normalL),
        buttonHover: hslToHex(hsl.h, hsl.s, hoverL),
        textColorNormal: getTextColorForBackground(normalL),
        textColorHover: getTextColorForBackground(hoverL)
    };
}


export function hexToHSL(H) {
    let r = 0, g = 0, b = 0;
    if (H.length === 7) {
        r = parseInt(H.substring(1, 3), 16) / 255;
        g = parseInt(H.substring(3, 5), 16) / 255;
        b = parseInt(H.substring(5, 7), 16) / 255;
    }
    let max = Math.max(r, g, b), min = Math.min(r, g, b);
    let h = 0, s = 0, l = (max + min) / 2;

    if (max !== min) {
        let d = max - min;
        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
        switch (max) {
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }
        h /= 6;
    }

    h = Math.round(h * 360);
    s = Math.round(s * 100);
    l = Math.round(l * 100);

    return { h, s, l };
}

export function hslToHex(h, s, l) {
    s /= 100;
    l /= 100;

    function hue2rgb(p, q, t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1 / 6) return p + (q - p) * 6 * t;
        if (t < 1 / 2) return q;
        if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
        return p;
    }

    let r, g, b;

    if (s === 0) {
        r = g = b = l;
    } else {
        let q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        let p = 2 * l - q;
        r = hue2rgb(p, q, h / 360 + 1 / 3);
        g = hue2rgb(p, q, h / 360);
        b = hue2rgb(p, q, h / 360 - 1 / 3);
    }

    let toHex = x => {
        let hex = Math.round(x * 255).toString(16);
        return hex.length === 1 ? "0" + hex : hex;
    };

    return "#" + toHex(r) + toHex(g) + toHex(b);
}