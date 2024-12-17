import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Day17 {
    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    private static final String TEST_INPUT = """
            Register A: 729
            Register B: 0
            Register C: 0
                        
            Program: 0,1,5,4,3,0
            """;

    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    private static final String TEST_INPUT2 = """
            Register A: 2024
            Register B: 0
            Register C: 0
                        
            Program: 0,3,5,4,3,0
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(17);
        String realInput = AocUtils.download(17);

        partI(TEST_INPUT);
        partII(TEST_INPUT2);
        System.out.println();

        partI(realInput);
        partII(realInput);
    }

    private static void partI(String input) {
        Parsed parsed = parse(input);
        List<Integer> out = run(parsed.program(), parsed.memory());
        String res = String.join(",", out.stream().map(String::valueOf).toList());
        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        Parsed parsed = parse(input);
        List<Long> found = find(0, 0, parsed.program(), parsed.memory());
        long res = Collections.min(found);
        System.out.println("Part II: " + res);
    }

    private static Parsed parse(String input) {
        List<String> lines = input.lines().toList();
        Memory memory = new Memory();
        memory.a = Integer.parseInt(lines.get(0).split(" ")[2]);
        memory.b = Integer.parseInt(lines.get(1).split(" ")[2]);
        memory.c = Integer.parseInt(lines.get(2).split(" ")[2]);
        List<Integer> program = Stream.of(lines.get(4).substring(9).split(",")).map(Integer::parseInt).toList();
        return new Parsed(memory, program);
    }

    private static List<Integer> run(List<Integer> program, Memory memory) {
        List<Integer> out = new ArrayList<>();

        while (memory.instructionPointer < program.size()) {
            execute(memory.opCode(program), memory.operand(program), memory, out);
        }

        return out;
    }

    private static void execute(OpCode opCode, long operand, Memory mem, List<Integer> out) {
        switch (opCode) {
            case ADV -> mem.a /= (1L << mem.combo(operand));
            case BXL -> mem.b = mem.b ^ operand;
            case BST -> mem.b = AocUtils.modulo(mem.combo(operand), 8);
            case JNZ -> {
                if (mem.a != 0) {
                    mem.instructionPointer = (int) operand;
                    return;
                }
            }
            case BXC -> mem.b = mem.b ^ mem.c;
            case OUT -> out.add((int) AocUtils.modulo(mem.combo(operand), 8));
            case BDV -> mem.b = mem.a / (1L << mem.combo(operand));
            case CDV -> mem.c = mem.a / (1L << mem.combo(operand));
        }

        mem.instructionPointer += 2;
    }

    private static List<Long> find(long prefix, int depth, List<Integer> program, Memory memory) {
        List<Long> found = new ArrayList<>();

        for (long a = prefix * 8; a < (prefix + 1) * 8; a++) {
            List<Integer> out = run(program, memory.withA(a));

            if (out.equals(program)) {
                found.add(a);
            } else if (depth < program.size() && out.equals(program.subList(program.size() - depth - 1, program.size()))) {
                found.addAll(find(a, depth + 1, program, memory));
            }
        }

        return found;
    }

    private record Parsed(Memory memory, List<Integer> program) {}

    private enum OpCode {
        ADV, BXL, BST, JNZ, BXC, OUT, BDV, CDV
    }

    private static class Memory {
        private static final OpCode[] OP_CODES = OpCode.values();

        long a;
        long b;
        long c;
        int instructionPointer;

        OpCode opCode(List<Integer> program) {
            return OP_CODES[program.get(instructionPointer)];
        }

        long operand(List<Integer> program) {
            return program.get(instructionPointer + 1);
        }

        long combo(long operand) {
            return switch ((int) operand) {
                case 0 -> 0;
                case 1 -> 1;
                case 2 -> 2;
                case 3 -> 3;
                case 4 -> a;
                case 5 -> b;
                case 6 -> c;
                default -> throw new IllegalArgumentException();
            };
        }

        Memory withA(long newA) {
            Memory copy = new Memory();
            copy.a = newA;
            copy.b = b;
            copy.c = c;
            copy.instructionPointer = instructionPointer;
            return copy;
        }
    }
}
