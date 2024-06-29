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

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

/**
 * XML text node, wrapping a Saxon XdmNode
 *
 * @author Chris de Vreeze
 */
public final class TextNode extends XmlNode {

    public TextNode(XdmNode underlyingNode) {
        super(requiringTextNode(underlyingNode));
    }

    public String text() {
        return underlyingNode().getStringValue();
    }

    private static XdmNode requiringTextNode(XdmNode node) {
        if (!node.getNodeKind().equals(XdmNodeKind.TEXT)) {
            throw new IllegalArgumentException("Not a text node");
        }
        return node;
    }
}
