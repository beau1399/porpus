# Porpus - A Minimalist Leiningen Template for Web Development
* Server-Driven...
* But ClojureScript enabled for when you need it
* Suspicious of dependencies; HTTP is the framework...
* But "batteries" are nevertheless included
* Developer-friendly:
*  Tight development loop
*  __Friendly learning curve__

## Quick Start

This section will give you a quick path to a Porpus Web site, and show you what Porpus is and isn't. If you prefer to read about the design and intent of Porpus in greater depth, skip to the third major section of this document ("Porpus Design in Depth") and read that first. Otherwise, go ahead and follow these steps:

1. Get the Porpus repository onto your system
	cd ~
	mkdir porpus
	cd porpus
	git init
	git pull https://github.com/beau1399/porpus.git

2. Add Porpus to your Leiningen setup
	lein install

3. Make a Porpus projct
	cd ~
	lein new porpus-proj pptest

4. Run your project in debugging mode
	cd pptest
	lein figwheel

5. Browse to http://localhost:3449/seshtest and refresh a few times. You will see an incrementing counter on the page built around your session.

6. Browse to http://localhost:3449/seshtest2. You will see the same counter value as on the last page, plus one.

7. Browse to http://localhost:3449/parmtest?n=99, replacing "99" with different integer values. You will see each integer echoed back to you in the page body.

8. Browse to http://localhost:3449/parmtest?n=string. You will get an error page, since the "n" parameter is expected to be an integer.

9. Browse to http://localhost:3449/buttontest and click the "Say hi" button. You will see an alert dialog.

From this little demo, a few pieces of information about Porpus can be gleaned. First, note that __most of the action takes place on the server side.__ Things like React, the single-page architecture, etc. are not a part of Porpus, at least not out of the box.

Second, realize that this server-centric design does not prevent scripting from happening, as evidenced by the (__ClojureScript-driven__) alert dialog seen in step 9. 

Finally, a couple of key Web capabilites are evident in this basic demo: __typed querystring parameters__ and __session persistence__.

## Dissecting the Demo

## Porpus Design in Depth

Porpus is designed to get your Clojure-based Web development efforts going as quickly and unobtrusively as possible. It grew out of my own needs, and out of a couple of competing factors I perceived in my own efforts to develop in the language:

__1. Clojure is powerful, expressive, and works with (not against) real, contemporary hardware.
2. Even for Lisp veterans, learning to apply Clojure is made difficult by the ecosystems most typical of Clojure Web development__

### The Power of Clojure

Expounding upon #1 above, one can start with the pretty tired old argument that homoiconicity, macros, and so on [make Lisp a better language than the ones that most people are using](http://www.paulgraham.com/avg.html). Truth and theory aside, 1) this argument has been slow to resonate with people actually writing code, and 2) there's a lot that can be added to it.

In fact, Robert C. Martin has gone so far as to claim that ["Clojure is the new C"](https://www.infoq.com/presentations/clojure-c). What does this mean? In his talk, Martin focused a lot on the disciplined, terse, symbol-heavy nature of the syntax of both languages, and there are plenty of people out there who'll tell you that he's "missing the point."

What rings true to me about the statement that "Clojure is the new C," though, is the glaring *need* for a new version of what C once was, and the potential that this might be filled by something like Clojure. The days when C was a useful model of how typical hardware actually operated are behind us- hence another provactively-titled link telling us that ["C is no longer a low level language](https://queue.acm.org/detail.cfm?id=3212479), subtitled "your computer is not a fast PDP-11."

Consider the computer you are probably reading this on. It probably has a CPU that uses things like branch prediction, register renaming engines, exquisitely complex caching mechanisms, and the like to present the carefully-crafted, somewhat leaky abstraction that it really is just a fast PDP-11. Running alongside this CPU is a GPU that much more closely resembles what computer engineers would have computer scientists working on nowadays if they had their druthers. 

Over time this compromised engineering will only grow leakier in its abstractions, more prone to nasty surprises, and overall less sustainable. There is a need for the engineering of software to better harmonize with the engineering of hardware. 

### The Banality of Dumpster Diving


http://beauscode.blogspot.com/2013/02/language-and-development-tool-for.html
 inherent side effects like i/o
 less fundamental side effects like variable assignment




what if learning C meant also learning... CS education

Tight dev loop
Batteries included

## Dissecting the Demo
