/*
 * Copyright 2023-2024 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.tryjava.tryhttp.server;

import eu.cdevreeze.tryjava.tryhttp.model.Quote;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

/**
 * Hardcoded collection of quotes.
 *
 * @author Chris de Vreeze
 */
public class QuoteCollection {

    public static Seq<Quote> allQuotes = Vector.of(
            new Quote(
                    "If you can learn how to use your mind, anything is possible.",
                    "Wim Hof",
                    Vector.of("inner strength")),
            new Quote(
                    "I'm not afraid of dying. I'm afraid not to have lived.",
                    "Wim Hof",
                    Vector.of("inner strength")),
            new Quote("""
                    I've come to understand that if you want to learn something badly enough,
                    you'll find a way to make it happen.
                    Having the will to search and succeed is very important""",
                    "Wim Hof",
                    Vector.of("inner strength")),
            new Quote("""
                    In nature, it is not only the physically weak but the mentally weak that get eaten.
                    Now we have created this modern society in which we have every comfort,
                    yet we are losing our ability to regulate our mood, our emotions.""",
                    "Wim Hof",
                    Vector.of("inner strength")),
            new Quote("""
                    Cold is a stressor, so if you are able to get into the cold and control your body's response to it,
                    you will be able to control stress.""",
                    "Wim Hof",
                    Vector.of("inner strength")),
            new Quote("""
                    Justifying conscription to promote the cause of liberty is one of the most bizarre notions ever conceived by man!
                    Forced servitude, with the risk of death and serious injury as a price to live free, makes no sense.""",
                    "Ron Paul",
                    Vector.of("liberty")),
            new Quote("""
                    When the federal government spends more each year than it collects in tax revenues,
                    it has three choices: It can raise taxes, print money, or borrow money.
                    While these actions may benefit politicians, all three options are bad for average Americans.""",
                    "Ron Paul",
                    Vector.of("liberty")),
            new Quote("""
                    Well, I don't think we should go to the moon.
                    I think we maybe should send some politicians up there.""",
                    "Ron Paul",
                    Vector.of("politics")),
            new Quote("""
                    I think a submarine is a very worthwhile weapon.
                    I believe we can defend ourselves with submarines and all our troops back at home.
                    This whole idea that we have to be in 130 countries and 900 bases...
                    is an old-fashioned idea.""",
                    "Ron Paul",
                    Vector.of("liberty")),
            new Quote("""
                    Of course I've already taken a very modest position on the monetary system,
                    I do take the position that we should just end the Fed.""",
                    "Ron Paul",
                    Vector.of("liberty", "financial system")),
            new Quote("""
                    Legitimate use of violence can only be that which is required in self-defense.""",
                    "Ron Paul", Vector.of("defense")),
            new Quote("""
                    I am absolutely opposed to a national ID card.
                    This is a total contradiction of what a free society is all about.
                    The purpose of government is to protect the secrecy and the privacy of all individuals,
                    not the secrecy of government. We don't need a national ID card.""",
                    "Ron Paul", Vector.of("liberty")),
            new Quote("""
                    Maybe we ought to consider a Golden Rule in foreign policy:
                    Don't do to other nations what we don't want happening to us.
                    We endlessly bomb these countries and then we wonder why they get upset with us?""",
                    "Ron Paul", Vector.of("liberty", "peace")),
            new Quote("""
                    I am just absolutely convinced that the best formula for giving us peace and
                    preserving the American way of life is freedom, limited government,
                    and minding our own business overseas.""",
                    "Ron Paul", Vector.of("liberty", "peace")),
            new Quote("""
                    Real patriotism is a willingness to challenge the government when it's wrong.""",
                    "Ron Paul", Vector.of("patriotism", "liberty")),
            new Quote("""
                    Believe me, the intellectual revolution is going on,
                    and that has to come first before you see the political changes.
                    That's where I'm very optimistic.""",
                    "Ron Paul", Vector.of("politics")),
            new Quote("""
                    War is never economically beneficial except for those in position to profit from war expenditures.""",
                    "Ron Paul",
                    Vector.of("war", "profit")),
            new Quote("""
                    There is only one kind of freedom and that's individual liberty.
                    Our lives come from our creator and our liberty comes from our creator.
                    It has nothing to do with government granting it.""",
                    "Ron Paul", Vector.of("liberty"))
    );
}
