package org.verohallinto.apitamoclient.yleiset;

import javax.xml.namespace.NamespaceContext;
import java.util.*;

/**
 * <p>Sanomien XPath-käsittelyn Namespace-apuluokka.</p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class Nimiavaruus implements NamespaceContext {

    private Map<String, String> map;

    public Nimiavaruus() {

        map = new HashMap<>();
    }

    public void setNamespace(final String prefix, final String nsURI) {

        map.put(prefix, nsURI);
    }

    public void setNamespaces(final Map<String, String> nsURIs) {

        if (!map.isEmpty()) {
            map.clear();
        }

        map.putAll(nsURIs);
    }

    @Override
    public String getNamespaceURI(final String prefix) {

        return map.get(prefix);
    }

    @Override
    public String getPrefix(final String nsURI) {

        for (String prefix : map.keySet()) {
            String uri = map.get(prefix);

            if (uri.equals(nsURI)) {
                return prefix;
            }
        }

        return null;
    }

    @Override
    public Iterator getPrefixes(final String nsURI) {

        final List<String> prefixes = new ArrayList<>();

        for (String prefix : map.keySet()) {
            String uri = map.get(prefix);

            if (uri.equals(nsURI)) {
                prefixes.add(prefix);
            }
        }

        return prefixes.iterator();
    }
}
