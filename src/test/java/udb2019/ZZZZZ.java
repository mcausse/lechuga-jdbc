package udb2019;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.junit.Test;

public class ZZZZZ {

    @Test
    public void testName() throws Exception {

        int N = 299;
        Random rnd = new Random(0L);
        long[] nums = new long[N];
        for (int i = 0; i < N; i++) {
            nums[i] = Math.abs(rnd.nextLong() % N);
        }

        MockFileManager fm = new MockFileManager();
        Index<String> ix = fm.loadIndex();

        System.out.println(ix);
        for (long i : nums) {
            ix.put("" + i, i);
        }
        System.out.println(ix);

        for (long i : nums) {
            assertEquals(i, ix.get("" + i));
        }

        fm.storeIndex(ix);
    }

    @Test
    public void testName2() throws Exception {

        int N = 50;

        MockFileManager fm = new MockFileManager();
        Index<String> ix = fm.loadIndex();

        System.out.println(ix);
        for (long i = 0; i < N; i++) {
            ix.put("" + i, i);
        }
        System.out.println(ix);

        for (long i = 0; i < N; i++) {
            assertEquals(i, ix.get("" + i));
        }

        assertEquals("{3=3, 2=2, 20=20, 21=21, 22=22, 23=23, 24=24, 25=25, 26=26, 27=27, 28=28, 29=29}",
                ix.getIn("2", "3").toString());
        ix.remove("25");
        assertEquals("{3=3, 2=2, 20=20, 21=21, 22=22, 23=23, 24=24, 26=26, 27=27, 28=28, 29=29}",
                ix.getIn("2", "3").toString());

        fm.storeIndex(ix);
    }

    public static interface FileManager<K extends Comparable<K>> {

        Index<K> loadIndex();

        void storeIndex(Index<K> index);

        IndexChunk<K> loadIndexChunk(int numChunk);

        void storeIndexChunk(int numChunk, IndexChunk<K> chunk);
    }

    public static class MockFileManager implements FileManager<String> {

        Index<String> index;
        Map<Integer, IndexChunk<String>> chunks;

        public MockFileManager() {
            super();
            this.index = new Index<>(5, this);
            this.chunks = new LinkedHashMap<>();

            this.chunks.put(0, new IndexChunk<>());
        }

        @Override
        public Index<String> loadIndex() {
            return index;
        }

        @Override
        public void storeIndex(Index<String> index) {
            this.index = index;
        }

        @Override
        public IndexChunk<String> loadIndexChunk(int numChunk) {
            return chunks.get(numChunk);
        }

        @Override
        public void storeIndexChunk(int numChunk, IndexChunk<String> chunk) {
            chunks.put(numChunk, chunk);
        }

    }

    /**
     * @param <K>
     *            tipus de la clau
     */
    public static class Index<K extends Comparable<K>> {

        final int maxEntriesPerChunk;
        final FileManager<K> fileManager;

        /**
         * si val {@code null} val -INF
         */
        final List<K> minKeys;
        /**
         * si val {@code null} val +INF
         */
        final List<K> maxKeys;

        public Index(int maxEntriesPerChunk, FileManager<K> fileManager) {
            super();
            this.maxEntriesPerChunk = maxEntriesPerChunk;
            this.fileManager = fileManager;

            this.minKeys = new ArrayList<>();
            this.maxKeys = new ArrayList<>();

            this.minKeys.add(null);
            this.maxKeys.add(null);
        }

        protected int find(K key) {
            for (int i = 0; i < minKeys.size(); i++) {

                K minkey = minKeys.get(i);
                K maxkey = maxKeys.get(i);

                int c = 0;
                if (minkey == null || ComparableUtils.le(minkey, key)) {
                    c++;
                }
                if (maxkey == null || ComparableUtils.lt(key, maxkey)) {
                    c++;
                }
                if (c == 2) {
                    return i;
                }
            }
            throw new RuntimeException(
                    "internal error: sempre ha d'haver-hi un rang que contingui la clau (null aka +/-INF): " + key);
        }

        protected List<Integer> find(K fromKey, K toKey) {
            List<Integer> r = new ArrayList<>();
            for (int i = 0; i < minKeys.size(); i++) {
                K minkey = minKeys.get(i);
                K maxkey = maxKeys.get(i);

                int c = 0;
                if ((minkey == null || ComparableUtils.le(minkey, fromKey)) &&
                /**/(maxkey == null || ComparableUtils.lt(fromKey, maxkey))) {
                    c++;
                }
                if ((minkey == null || ComparableUtils.le(minkey, toKey)) &&
                /**/(maxkey == null || ComparableUtils.lt(toKey, maxkey))) {
                    c++;
                }

                if ((minkey == null || ComparableUtils.le(fromKey, minkey)) &&
                /**/(minkey == null || ComparableUtils.lt(minkey, toKey))) {
                    c++;
                }
                if ((maxkey == null || ComparableUtils.le(fromKey, maxkey)) &&
                /**/(maxkey == null || ComparableUtils.lt(maxkey, toKey))) {
                    c++;
                }

                if (c > 0) {
                    r.add(i);
                }
            }
            return r;
        }

        public void remove(K key) {
            int numChunk = find(key);
            IndexChunk<K> chunk = fileManager.loadIndexChunk(numChunk);
            chunk.remove(key);
        }

        public void put(K key, long value) {
            int numChunk = find(key);
            IndexChunk<K> chunk = fileManager.loadIndexChunk(numChunk);

            chunk.put(key, value);
            if (chunk.getSize() <= this.maxEntriesPerChunk) {
                fileManager.storeIndexChunk(numChunk, chunk);
            } else {
                // splitta en 2 chunks

                IndexChunk<K> chunk1 = new IndexChunk<>();
                IndexChunk<K> chunk2 = new IndexChunk<>();

                int med = chunk.getSize() / 2;
                K kmed = null;

                int c = 0;
                for (Entry<K, Long> e : chunk.getEntries().entrySet()) {

                    if (c < med) {
                        chunk1.put(e.getKey(), e.getValue());
                    } else {
                        if (c == med) {
                            kmed = e.getKey();
                        }
                        chunk2.put(e.getKey(), e.getValue());
                    }
                    c++;
                }

                if (kmed == null) {
                    throw new RuntimeException();
                }

                K minKey1 = minKeys.get(numChunk);
                K maxKey1 = kmed;
                K minKey2 = kmed;
                K maxKey2 = maxKeys.get(numChunk);

                fileManager.storeIndexChunk(numChunk, chunk1);
                minKeys.set(numChunk, minKey1);
                maxKeys.set(numChunk, maxKey1);

                fileManager.storeIndexChunk(minKeys.size(), chunk2);
                minKeys.add(minKey2);
                maxKeys.add(maxKey2);
            }

        }

        public long get(K key) {
            int numChunk = find(key);
            IndexChunk<K> chunk = fileManager.loadIndexChunk(numChunk);
            if (!chunk.getEntries().containsKey(key)) {
                throw new RuntimeException(String.valueOf(key));
            }
            return chunk.get(key);
        }

        public Map<K, Long> getIn(K fromKey, K toKey) {
            Map<K, Long> r = new LinkedHashMap<>();
            List<Integer> numChunks = find(fromKey, toKey);
            for (int numChunk : numChunks) {
                IndexChunk<K> chunk = fileManager.loadIndexChunk(numChunk);
                for (Entry<K, Long> e : chunk.getEntries().entrySet()) {

                    int c = 0;
                    K key = e.getKey();
                    if (fromKey == null || ComparableUtils.le(fromKey, key)) {
                        c++;
                    }
                    if (toKey == null || ComparableUtils.le(key, toKey)) {
                        c++;
                    }
                    if (c == 2) {
                        r.put(e.getKey(), e.getValue());
                    }

                }
            }
            return r;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < minKeys.size(); i++) {
                s.append("chunk #" + i + ": ");
                s.append(minKeys.get(i));
                s.append("..");
                s.append(maxKeys.get(i));
                s.append(": \t");
                IndexChunk<K> chunk = fileManager.loadIndexChunk(i);
                s.append(chunk);
                s.append("\n");
            }
            return s.toString();
        }
    }

    public static class IndexChunk<K extends Comparable<K>> {

        final Map<K, Long> entries;
        boolean pristine = true;

        public IndexChunk() {
            super();
            this.entries = new TreeMap<>();
        }

        public int getSize() {
            return entries.size();
        }

        public void put(K key, long value) {
            entries.put(key, value);
            pristine = false;
        }

        public void remove(K key) {
            entries.remove(key);
            pristine = false;
        }

        public long get(K key) {
            return entries.get(key);
        }

        public Map<K, Long> getEntries() {
            return entries;
        }

        public boolean isPristine() {
            return pristine;
        }

        @Override
        public String toString() {
            return this.entries.toString();
        }
    }

}
