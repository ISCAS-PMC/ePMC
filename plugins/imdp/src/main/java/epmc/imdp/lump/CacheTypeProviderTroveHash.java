package epmc.imdp.lump;

import java.util.Map;

import gnu.trove.map.hash.THashMap;

public final class CacheTypeProviderTroveHash implements CacheTypeProvider {
    public static String IDENTIFIER = "trove-hash";

    @Override
    public <K,V> Map<K,V> newMap() {
        return new THashMap<>();
    }

}
