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

package eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.model;

import java.util.Objects;

/**
 * Text node
 *
 * @param text
 * @author Chris de Vreeze
 */
public record TextNode(String text) implements XmlNode {
    public TextNode {
        Objects.requireNonNull(text);
    }
}
