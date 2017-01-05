A CHIP-8 interpreter written in Java
-------------------------------------------------------

A project implementing a [CHIP-8 interpreter](https://en.wikipedia.org/wiki/CHIP-8). ROMs with the .ch8 and .rom extensions can be loaded into the memory, and many of these ROMs can be downloaded from (http://www.zophar.net/pdroms/chip8/chip-8-games-pack.html). The original CHIP-8 had a keypad with the numbered keys 0 - 9 and A - F (16 keys in total).

-----------------------------------------------------------------------


<h3><a id="user-content-mapping-chip-8-keypad-to-pc-keyboard" class="anchor" href="#mapping-chip-8-keypad-to-pc-keyboard" aria-hidden="true"><svg aria-hidden="true" class="octicon octicon-link" height="16" version="1.1" viewBox="0 0 16 16" width="16"><path d="M4 9h1v1h-1c-1.5 0-3-1.69-3-3.5s1.55-3.5 3-3.5h4c1.45 0 3 1.69 3 3.5 0 1.41-0.91 2.72-2 3.25v-1.16c0.58-0.45 1-1.27 1-2.09 0-1.28-1.02-2.5-2-2.5H4c-0.98 0-2 1.22-2 2.5s1 2.5 2 2.5z m9-3h-1v1h1c1 0 2 1.22 2 2.5s-1.02 2.5-2 2.5H9c-0.98 0-2-1.22-2-2.5 0-0.83 0.42-1.64 1-2.09v-1.16c-1.09 0.53-2 1.84-2 3.25 0 1.81 1.55 3.5 3 3.5h4c1.45 0 3-1.69 3-3.5s-1.5-3.5-3-3.5z"></path></svg></a>Mapping of CHIP-8 keypad to keyboard</h3>

<pre><code>Keypad                   Keyboard
+-+-+-+-+                +-+-+-+-+
|1|2|3|C|                |4|6|2|D|
+-+-+-+-+                +-+-+-+-+
|4|5|6|D|                |Q|W|E|Z|
+-+-+-+-+       =&gt;       +-+-+-+-+
|7|8|9|E|                |R|T|Y|X|
+-+-+-+-+                +-+-+-+-+
|A|0|B|F|                |A|8|S|C|
+-+-+-+-+                +-+-+-+-+
</code></pre>
