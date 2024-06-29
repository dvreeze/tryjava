/*
 * Copyright 2024-2024 Chris de Vreeze
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

package eu.cdevreeze.tryjava.tryxml.saxon;

import com.google.common.collect.ImmutableMap;
import eu.cdevreeze.tryjava.tryxml.queryapi.ParentAwareElementQueryApi;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

import javax.xml.namespace.QName;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * XML element node, wrapping a Saxon XdmNode
 *
 * @author Chris de Vreeze
 */
public final class Element extends XmlNode implements ParentAwareElementQueryApi<Element> {

    private static final FunctionalSaxonElementQueryApi functionalElementQueryApi =
            FunctionalSaxonElementQueryApi.instance;

    public Element(XdmNode underlyingNode) {
        super(requiringElementNode(underlyingNode));
    }

    @Override
    public QName elementName() {
        return functionalElementQueryApi.elementName(underlyingNode());
    }

    @Override
    public ImmutableMap<QName, String> attributes() {
        return functionalElementQueryApi.attributes(underlyingNode());
    }

    public Stream<XmlNode> childNodeStream() {
        return underlyingNode().axisIterator(Axis.CHILD).stream()
                .flatMap(n -> switch (n.getNodeKind()) {
                    case XdmNodeKind.ELEMENT -> Stream.of(new Element(n));
                    case XdmNodeKind.TEXT -> Stream.of(new TextNode(n));
                    case XdmNodeKind.COMMENT -> Stream.of(new Comment(n));
                    default -> Stream.empty();
                });
    }

    @Override
    public Stream<Element> childElementStream() {
        return functionalElementQueryApi.childElementStream(underlyingNode())
                .map(Element::new);
    }

    @Override
    public Stream<Element> childElementStream(Predicate<Element> predicate) {
        return childElementStream().filter(predicate);
    }

    @Override
    public Stream<Element> descendantElementOrSelfStream() {
        return functionalElementQueryApi.descendantElementOrSelfStream(underlyingNode())
                .map(Element::new);
    }

    @Override
    public Stream<Element> descendantElementOrSelfStream(Predicate<Element> predicate) {
        return descendantElementOrSelfStream().filter(predicate);
    }

    @Override
    public Stream<Element> descendantElementStream() {
        return functionalElementQueryApi.descendantElementStream(underlyingNode())
                .map(Element::new);
    }

    @Override
    public Stream<Element> descendantElementStream(Predicate<Element> predicate) {
        return descendantElementStream().filter(predicate);
    }

    @Override
    public Stream<Element> topmostDescendantElementOrSelfStream(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            return Stream.of(this);
        } else {
            // Recursion
            return childElementStream().flatMap(che -> che.topmostDescendantElementOrSelfStream(predicate));
        }
    }

    @Override
    public Stream<Element> topmostDescendantElementStream(Predicate<Element> predicate) {
        return childElementStream().flatMap(che -> che.topmostDescendantElementOrSelfStream(predicate));
    }

    @Override
    public Stream<Element> ancestorElementOrSelfStream() {
        return functionalElementQueryApi.ancestorElementOrSelfStream(underlyingNode())
                .map(Element::new);
    }

    @Override
    public Stream<Element> ancestorElementStream() {
        return functionalElementQueryApi.ancestorElementStream(underlyingNode())
                .map(Element::new);
    }

    @Override
    public Optional<Element> parentElementOption() {
        return functionalElementQueryApi.parentElementOption(underlyingNode())
                .map(Element::new);
    }

    private static XdmNode requiringElementNode(XdmNode node) {
        if (!node.getNodeKind().equals(XdmNodeKind.ELEMENT)) {
            throw new IllegalArgumentException("Not an element node");
        }
        return node;
    }
}
