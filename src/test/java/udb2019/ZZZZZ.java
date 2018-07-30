package udb2019;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
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

        for (long i : nums) {
            assertEquals(i, ix.get("" + i));
        }

        fm.storeIndex(ix);

        System.out.println(ix);
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

        for (long i = 0; i < N; i++) {
            assertEquals(i, ix.get("" + i));
        }

        assertEquals("{2=2, 20=20, 21=21, 22=22, 23=23, 24=24, 25=25, 26=26, 27=27, 28=28, 29=29, 3=3}",
                ix.getIn("2", "3").toString());
        ix.remove("25");
        assertEquals("{2=2, 20=20, 21=21, 22=22, 23=23, 24=24, 26=26, 27=27, 28=28, 29=29, 3=3}",
                ix.getIn("2", "3").toString());

        fm.storeIndex(ix);

        System.out.println(ix);
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
            Collections.sort(index.entries);
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

    public static class IndexEntry<K extends Comparable<K>> implements Comparable<IndexEntry<K>> {

        /**
         * si val {@code null} val -INF
         */
        K minKey;
        /**
         * si val {@code null} val +INF
         */
        K maxKey;

        int numChunk;

        public IndexEntry() {
            super();
        }

        public IndexEntry(K minKey, K maxKey, int numChunk) {
            super();
            this.minKey = minKey;
            this.maxKey = maxKey;
            this.numChunk = numChunk;
        }

        public K getMinKey() {
            return minKey;
        }

        public void setMinKey(K minKey) {
            this.minKey = minKey;
        }

        public K getMaxKey() {
            return maxKey;
        }

        public void setMaxKey(K maxKey) {
            this.maxKey = maxKey;
        }

        public int getNumChunk() {
            return numChunk;
        }

        public void setNumChunk(int numChunk) {
            this.numChunk = numChunk;
        }

        @Override
        public int compareTo(IndexEntry<K> o) {
            if (this.minKey == null) {
                return -1;
            }
            if (o.minKey == null) {
                return 1;
            }
            return this.minKey.compareTo(o.minKey);
        }

        @Override
        public String toString() {
            return "IndexEntry [minKey=" + minKey + ", maxKey=" + maxKey + ", numChunk=" + numChunk + "]";
        }

    }

    /**
     * @param <K>
     *            tipus de la clau
     */
    public static class Index<K extends Comparable<K>> {

        final int maxEntriesPerChunk;
        final FileManager<K> fileManager;

        final List<IndexEntry<K>> entries;

        public Index(int maxEntriesPerChunk, FileManager<K> fileManager) {
            super();
            this.maxEntriesPerChunk = maxEntriesPerChunk;
            this.fileManager = fileManager;

            this.entries = new ArrayList<>();
            this.entries.add(new IndexEntry<K>(null, null, 0));

        }

        protected int find(K key) {
            for (IndexEntry<K> entry : entries) {

                K minkey = entry.getMinKey();
                K maxkey = entry.getMaxKey();

                int c = 0;
                if (minkey == null || ComparableUtils.le(minkey, key)) {
                    c++;
                }
                if (maxkey == null || ComparableUtils.lt(key, maxkey)) {
                    c++;
                }
                if (c == 2) {
                    return entry.getNumChunk();
                }
            }
            throw new RuntimeException(
                    "internal error: sempre ha d'haver-hi un rang que contingui la clau (null aka +/-INF): " + key);
        }

        protected List<Integer> find(K fromKey, K toKey) {

            List<Integer> r = new ArrayList<>();

            for (IndexEntry<K> entry : entries) {

                K minkey = entry.getMinKey();
                K maxkey = entry.getMaxKey();

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
                    r.add(entry.getNumChunk());
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

                K minKey1 = entries.get(numChunk).getMinKey();
                K maxKey1 = kmed;
                K minKey2 = kmed;
                K maxKey2 = entries.get(numChunk).getMaxKey();

                fileManager.storeIndexChunk(numChunk, chunk1);
                entries.get(numChunk).setMinKey(minKey1);
                entries.get(numChunk).setMaxKey(maxKey1);

                fileManager.storeIndexChunk(entries.size(), chunk2);
                entries.add(new IndexEntry<K>(minKey2, maxKey2, entries.size()));
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
            Map<K, Long> r = new TreeMap<>();
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
            for (IndexEntry<K> entry : entries) {
                s.append("chunk #" + entry.getNumChunk() + ": ");
                s.append(entry.getMinKey());
                s.append("..");
                s.append(entry.getMaxKey());
                s.append(": \t");
                IndexChunk<K> chunk = fileManager.loadIndexChunk(entry.getNumChunk());
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
