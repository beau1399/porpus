# Porpus - A Minimalist Leiningen Template for Web Development

## Quick Start

This section will give you a quick path to a Porpus Web site, and show you what Porpus is and isn't. If you prefer to read about the design and intent of Porpus in greater depth, skip to the next section. Otherwise, go ahead and follow these steps:

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

From this little demo, a few pieces of information about Porpus can be gleaned. First, note that *most of the action takes place on the server side.* Things like React, the single-page architecture, etc. are not a part of Porpus, at least not out of the box.

Second, realize that this server-centric design does not prevent scripting from happening, as evidenced by the (ClojureScript-driven) alert dialog seen in step 9. 

Finally, a couple of key Web capabilites are evident in this basic demo: typed querystring parameters and session persistence.

## Porpus In Depth

Porpus is designed to get your Clojure-based Web development efforts going as quickly and unobtrusively as possible. It grew out of my own needs, and a couple of competing factors I perceived in my own efforts to develop in the language:

1. Clojure is powerful, expressive, and works with (not against) real, contemporary hardware.
2. Even for Lisp veterans, learning to apply Clojure is made difficult by the ecosystems most typical of Clojure Web development

Expounding upon #1 above, one can start with the pretty tired old argument that homoiconicity, macros, and so on [make Lisp a better language than the ones that most people are using](http://www.paulgraham.com/avg.html). Truth and theory aside, 1) this argument has been slow to resonate with people actually writing code, and 2) there's a lot that can be added to it.
