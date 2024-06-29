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
import eu.cdevreeze.tryjava.tryxml.functionalqueryapi.FunctionalParentAwareElementQueryApi;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Saxon XdmNode element query function API
 *
 * @author Chris de Vreeze
 */
public final class FunctionalSaxonElementQueryApi implements FunctionalParentAwareElementQueryApi<XdmNode> {

    public final static FunctionalSaxonElementQueryApi instance = new FunctionalSaxonElementQueryApi();

    private FunctionalSaxonElementQueryApi() {
    }

    public XdmNode getDocumentElement(XdmNode node) {
        return node.axisIterator(Axis.DESCENDANT_OR_SELF).stream()
                .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public QName elementName(XdmNode element) {
        return convertSaxonQNameToQName(requiringElementNode(element).getNodeName());
    }

    @Override
    public ImmutableMap<QName, String> attributes(XdmNode element) {
        return requiringElementNode(element).axisIterator(Axis.ATTRIBUTE).stream()
                .map(n -> Map.entry(convertSaxonQNameToQName(n.getNodeName()), n.getStringValue()))
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Stream<XdmNode> childElementStream(XdmNode element) {
        return requiringElementNode(element).axisIterator(Axis.CHILD).stream()
                .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT));
    }

    @Override
    public Stream<XdmNode> childElementStream(XdmNode element, Predicate<XdmNode> predicate) {
        return childElementStream(requiringElementNode(element)).filter(predicate);
    }

    @Override
    public Stream<XdmNode> descendantElementOrSelfStream(XdmNode element) {
        return requiringElementNode(element).axisIterator(Axis.DESCENDANT_OR_SELF).stream()
                .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT));
    }

    @Override
    public Stream<XdmNode> descendantElementOrSelfStream(XdmNode element, Predicate<XdmNode> predicate) {
        return descendantElementOrSelfStream(requiringElementNode(element)).filter(predicate);
    }

    @Override
    public Stream<XdmNode> descendantElementStream(XdmNode element) {
        return requiringElementNode(element).axisIterator(Axis.DESCENDANT).stream()
                .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT));
    }

    @Override
    public Stream<XdmNode> descendantElementStream(XdmNode element, Predicate<XdmNode> predicate) {
        return descendantElementStream(requiringElementNode(element)).filter(predicate);
    }

    @Override
    public Stream<XdmNode> topmostDescendantElementOrSelfStream(XdmNode element, Predicate<XdmNode> predicate) {
        if (predicate.test(element)) {
            return Stream.of(element);
        } else {
            // Recursion
            return childElementStream(element).flatMap(che -> topmostDescendantElementOrSelfStream(che, predicate));
        }
    }

    @Override
    public Stream<XdmNode> topmostDescendantElementStream(XdmNode element, Predicate<XdmNode> predicate) {
        return childElementStream(element).flatMap(che -> topmostDescendantElementOrSelfStream(che, predicate));
    }

    @Override
    public Stream<XdmNode> ancestorElementOrSelfStream(XdmNode element) {
        return requiringElementNode(element).axisIterator(Axis.ANCESTOR_OR_SELF).stream()
                .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT));
    }

    @Override
    public Stream<XdmNode> ancestorElementStream(XdmNode element) {
        return requiringElementNode(element).axisIterator(Axis.ANCESTOR).stream()
                .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT));
    }

    @Override
    public Optional<XdmNode> parentElementOption(XdmNode element) {
        return requiringElementNode(element).axisIterator(Axis.PARENT).stream()
                .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT))
                .findFirst();
    }

    private static XdmNode requiringElementNode(XdmNode node) {
        if (!node.getNodeKind().equals(XdmNodeKind.ELEMENT)) {
            throw new IllegalArgumentException("Not an element node");
        }
        return node;
    }

    private static QName convertSaxonQNameToQName(net.sf.saxon.s9api.QName saxonQName) {
        var structuredQName = saxonQName.getStructuredQName();

        var nsUri = structuredQName.getURI();
        var localName = structuredQName.getLocalPart();
        var prefix = structuredQName.getPrefix();
        return new QName(nsUri, localName, prefix);
    }
}
