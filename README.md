
Trying out Java again
=====================

Trying out Java (21), JDK APIs, functional programming, immutable collections etc.

Moving from Scala back to Java
------------------------------

### What I like about Scala

Going back from Scala to Java is a bit painful for me, in that I really like Scala as a language. Fortunately modern
versions of Java (such as Java 21) offer a far better developer experience than older versions. For example, with type
inference for local variables (using the "var" keyword) and record classes the amount of boilerplate code decreases
significantly.

What is it that I really like and benefited from in Scala? For example:

+ Thread-safe immutable collections
+ Great language for domain modeling
+ Supports functional effect systems such as ZIO 2, and "FP light" in general (writing deterministic total pure functions)
+ Term derivation by the compiler ("implicits"), in particular the improved "implicits" in Scala 3

This helped me in the past. Thread-safe immutable collections are great when developing an XML library (which I did with
"yaidom"), that must support in-memory collections of read-only XML "DOM" trees shared by multiple threads.

I used Scala's domain modeling strengths when developing XBRL support (on top of above-mentioned XML library). Tnis
combination of bottom-up domain modeling and top-down application development on top of that domain enables very quick
(XBRL) application development once the coded domain is there.

I like functional effect systems like ZIO 2 a lot, but I don't know about many projects using it. Even Scala projects in
general are a bit scarce, compared to Java projects. But "FP light", or the habit of moving towards the usage of
deterministic total pure functions, certainly helps create programs that work, and that support *local reasoning* about
code. This habit goes well together with using immutable data structures and immutable collections in particular.

Scala "implicits" (when used in moderation) are a compile-time safe alternative to many applications of Java reflection.

How would Java (at least in theory) stack up nowadays against my points above about what I like about Scala? Not too bad:

+ Guava (or alternative libraries) offer (widely used) thread-safe immutable collections to Java
+ With record classes, local variable type inference, etc., Java has evolved into a much improved domain modeling language
+ I don't know about functional effect systems in Java, but "FP light" is well supported now, e.g. due to record types
+ Java has no "implicits", so in Java we are stuck with reflection

So other than implicits versus reflection, Java does fine in the points that I like and benefited from in Scala.

### Different philosophies of Scala and Java

Of course, the philosophies behind Scala and Java differ. In spirit, Scala is not married to the JVM. It tries to marry
(modern) OO with FP in a statically typed language, offering a sound orthogonal set of language features, backed by an
advanced type system with a strong theoretical foundation. You could say that Scala equally values nouns and verbs
(instead of being Steve Yegge's "kingdom of nouns"). Note that Scala's perception of OO is not an endorsement of
mutability-by-default.

Java, on the other hand, is more closely bound to the JVM, and therefore less orthogonal. Where do we see that? E.g.:

+ We extend traits in Scala, but in Java we extend classes yet implement interfaces
+ In Java we have static members, that we cannot abstract about, whereas in Scala singleton objects can extend traits
+ In Java primitives and (heap) objects are clearly distinct, whereas in Scala this distinction is less prominent
+ In Java methods and operators are distinct concepts, but in Scala "operators" are just methods with different names

### Ideas about programming in general

In general, some things I find important in programming:

+ Ability to *reason locally about code*
+ A clear "mental model" of the *runtime behaviour/assumptions* of a framework/library (e.g. ZIO 2, Futures, servlets, etc.)
+ Disciplined use of packages/namespaces, with (almost) only *unidirectional dependencies*
+ On DRY versus unwanted dependencies: undesirable (often ad-hoc) dependencies hurt more than some code repetition

As an example of different runtime behaviour and hidden assumptions is ThreadLocal in one-thread-per-request servlets used in
combination with Futures that take an underlying ExecutionContext ("breaking" the ThreadLocals).

In summary, what I find important in programming is mostly related to *discipline*, to limiting myself to practices that
work well at a larger scale.

