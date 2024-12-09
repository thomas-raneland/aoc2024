import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day09 {
    private static final String INPUT = "2333133121414131402";

    public static void main(String... args) {
        partI();
        partII();
    }

    private static void partI() {
        List<Block> blocks = toBlocks(parse());
        defragBlocks(blocks);
        long checksum = checksum(blocks);
        System.out.println("Part I: " + checksum);
    }

    private static void partII() {
        List<BlockSequence> sequences = parse();
        defragSequences(sequences);
        long checksum = checksum(toBlocks(sequences));
        System.out.println("Part II: " + checksum);
    }

    private static List<BlockSequence> parse() {
        List<BlockSequence> sequences = new ArrayList<>();
        int id = 0;
        int start = 0;
        boolean free = false;

        for (int pos = 0; pos < INPUT.length(); pos++) {
            int length = Integer.parseInt(INPUT.substring(pos, pos + 1));
            int end = start + length;
            sequences.add(new BlockSequence(free ? Block.FREE.id() : id++, start, end));
            start = end;
            free = !free;
        }

        return sequences;
    }

    private static List<Block> toBlocks(List<BlockSequence> sequences) {
        List<Block> blocks = new ArrayList<>();

        for (var sequence : sequences) {
            Block block = sequence.isFree() ? Block.FREE : new Block(sequence.id());
            Stream.generate(() -> block).limit(sequence.length()).forEach(blocks::add);
        }

        return blocks;
    }

    private static void defragBlocks(List<Block> blocks) {
        for (int pos = 0; pos < blocks.size(); pos++) {
            if (blocks.get(pos).isFree()) {
                int endPos = lastNonFree(blocks);

                if (endPos < pos) {
                    break;
                }

                Collections.swap(blocks, pos, endPos);
            }
        }
    }

    private static int lastNonFree(List<Block> blocks) {
        for (int pos = blocks.size() - 1; pos >= 0; pos--) {
            if (!blocks.get(pos).isFree()) {
                return pos;
            }
        }

        return -1;
    }

    private static long checksum(List<Block> blocks) {
        return IntStream.range(0, blocks.size())
                        .filter(pos -> !blocks.get(pos).isFree())
                        .mapToLong(pos -> pos * (long) blocks.get(pos).id())
                        .sum();
    }

    private static void defragSequences(List<BlockSequence> sequences) {
        List<BlockSequence> files = new ArrayList<>();
        List<BlockSequence> free = new ArrayList<>();
        sequences.forEach(sequence -> (sequence.isFree() ? free : files).add(sequence));
        sequences.clear();

        for (var file : files.reversed()) {
            for (var space : free) {
                if (space.start() >= file.start()) {
                    sequences.add(file);
                    break;
                } else if (file.length() <= space.length()) {
                    sequences.add(file.movedTo(space.start()));
                    free.remove(space);
                    free.add(file.freed());

                    if (file.length() < space.length()) {
                        free.add(space.withoutInitial(file.length()));
                    }

                    free.sort(Comparator.comparing(BlockSequence::start));
                    break;
                }
            }
        }

        sequences.addAll(free);
        sequences.sort(Comparator.comparing(BlockSequence::start));
    }

    record BlockSequence(int id, int start, int end) {
        public int length() {
            return end - start;
        }

        public boolean isFree() {
            return id == Block.FREE.id();
        }

        public BlockSequence movedTo(int start) {
            return new BlockSequence(id, start, start + length());
        }

        public BlockSequence freed() {
            return new BlockSequence(Block.FREE.id(), start, end);
        }

        public BlockSequence withoutInitial(int initialLength) {
            return new BlockSequence(id, start + initialLength, end);
        }
    }

    record Block(int id) {
        static final Block FREE = new Block(-1);

        @Override
        public String toString() {
            return isFree() ? "." : id < 10 ? String.valueOf(id) : "(" + id + ")";
        }

        public boolean isFree() {
            return id == FREE.id();
        }
    }
}
