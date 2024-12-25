import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day24 {
    public static void main(String... args) {
        AocUtils.waitForStartTime(24);
        String realInput = AocUtils.download(24);
        partI(realInput);
        partII(realInput);
    }

    private static void partI(String input) {
        String[] parts = input.split("\n\n");
        Device device = Device.parse(parts[1]);
        long output = device.output(Input.parse(parts[0]));
        System.out.println("Part I: " + output);
    }

    private static void partII(String input) {
        Program expected = expected();
        String[] parts = input.split("\n\n");
        Device device = Device.parse(parts[1]);
        Set<String> candidates = candidates(expected, device);
        List<Pair> pairs = Pair.from(candidates);
        Input testInput = Input.of(123456789L, 123456789L);
        long testOutput = 2 * 123456789L;

        for (var combination : Pair.combinations(pairs)) {
            Device swapped = device;

            for (Pair pair : combination) {
                swapped = swapped.swap(pair.o1, pair.o2);
            }

            if (!swapped.isCircular() && swapped.output(testInput) == testOutput && expected.equivalent(swapped.program())) {
                List<String> found = combination.stream().flatMap(Pair::stream).sorted().toList();
                System.out.println("Part II: " + String.join(",", found));
                return;
            }
        }

        System.out.println("Part II: No solution found");
    }

    private static Program expected() {
        Map<String, Expr> expressions = new LinkedHashMap<>();
        expressions.put("z00", xor(var("x00"), var("y00")));
        expressions.put("z00-AND", and(var("x00"), var("y00")));
        expressions.put("z01", xor(xor(var("x01"), var("y01")), var("z00-AND")));
        expressions.put("z01-AND", and(xor(var("x01"), var("y01")), var("z00-AND")));

        for (int i = 2; i < 45; i++) {
            String x = "x" + (i < 10 ? "0" + i : i);
            String y = "y" + (i < 10 ? "0" + i : i);
            String z = "z" + (i < 10 ? "0" + i : i);
            int prevI = i - 1;
            String prevX = "x" + (prevI < 10 ? "0" + prevI : prevI);
            String prevY = "y" + (prevI < 10 ? "0" + prevI : prevI);
            String prevZ = "z" + (prevI < 10 ? "0" + prevI : prevI);
            expressions.put(z, xor(xor(var(x), var(y)), or(var(prevZ + "-AND"), and(var(prevX), var(prevY)))));
            expressions.put(z + "-AND", and(xor(var(x), var(y)), or(var(prevZ + "-AND"), and(var(prevX), var(prevY)))));
        }

        expressions.put("z45", or(and(var("x44"), var("y44")), var("z44-AND")));
        return new Program(expressions);
    }

    private static Set<String> candidates(Program expected, Device actual) {
        Set<String> candidates = new HashSet<>();
        Program program = actual.program();

        for (Gate gate : actual.outputGates()) {
            if (!expected.equivalent(gate.out(), program)) {
                Expr expectedExpr = expected.expr(gate.out());
                Expr actualExpr = program.expr(gate.out());
                Expr inlineDiffActual = program.inline(minimalDiff(expectedExpr, actualExpr));

                for (Gate g : actual.allGates()) {
                    if (program.inline(actual.expr(g.out())).equals(inlineDiffActual)) {
                        candidates.add(g.out());
                    }
                }
            }
        }

        // hack, found jct manually by looking at the expressions for each output gate after substitution
        candidates.add("jct");
        return candidates;
    }

    private static Expr minimalDiff(Expr expected, Expr actual) {
        if (expected instanceof BinaryExpr be && actual instanceof BinaryExpr ba && be.op == ba.op) {
            for (Expr eOpnd : be.operands()) {
                for (Expr aOpnd : ba.operands()) {
                    if (eOpnd.equals(aOpnd)) {
                        Expr notE = be.operands().stream().filter(x -> !x.equals(eOpnd)).findFirst().orElse(null);
                        Expr notA = ba.operands().stream().filter(x -> !x.equals(aOpnd)).findFirst().orElse(null);
                        return minimalDiff(notE, notA);
                    }
                }
            }
        }

        return actual;
    }

    private static BinaryExpr xor(Expr a, Expr b) {
        return new BinaryExpr(Set.of(a, b), Op.XOR);
    }

    private static BinaryExpr and(Expr a, Expr b) {
        return new BinaryExpr(Set.of(a, b), Op.AND);
    }

    private static BinaryExpr or(Expr a, Expr b) {
        return new BinaryExpr(Set.of(a, b), Op.OR);
    }

    private static Variable var(String name) {
        return new Variable(name);
    }

    private record Device(Map<String, Gate> gates) {
        static Device parse(String definition) {
            Map<String, Gate> gates = new HashMap<>();

            for (String line : definition.lines().toList()) {
                Gate gate = Gate.parse(line);
                gates.put(gate.out, gate);
            }

            return new Device(gates);
        }

        long output(Input input) {
            String zString = outputGates()
                    .stream()
                    .map(Gate::out)
                    .sorted(Comparator.reverseOrder())
                    .map(out -> String.valueOf(compute(out, input)))
                    .collect(Collectors.joining());

            return Long.parseLong(zString, 2);
        }

        private int compute(String name, Input input) {
            if (name.startsWith("x") || name.startsWith("y")) {
                return input.values().getOrDefault(name, 0);
            }

            Gate gate = gates.get(name);
            return gate.op().value(compute(gate.a(), input), compute(gate.b(), input));
        }

        boolean isCircular() {
            return allGates().stream().anyMatch(g -> g.isCircular(gates));
        }

        Device swap(String lhs, String rhs) {
            Map<String, Gate> swapped = new HashMap<>();

            gates.forEach((name, gate) -> {
                if (name.equals(lhs)) {
                    swapped.put(rhs, new Gate(gate.op(), gate.a(), gate.b(), rhs));
                } else if (name.equals(rhs)) {
                    swapped.put(lhs, new Gate(gate.op(), gate.a(), gate.b(), lhs));
                } else {
                    swapped.put(name, gate);
                }
            });

            return new Device(swapped);
        }

        List<Gate> allGates() {
            return new ArrayList<>(gates.values());
        }

        List<Gate> outputGates() {
            return allGates().stream().filter(g -> g.out().startsWith("z")).toList();
        }

        Expr expr(String out) {
            Gate g = gates.get(out);
            return g != null ? new BinaryExpr(Set.of(expr(g.a()), expr(g.b())), g.op()) : new Variable(out);
        }

        Program program() {
            Map<String, Expr> expressions = new LinkedHashMap<>();

            for (Gate g : outputGates().stream().sorted(Comparator.comparing(Gate::out)).toList()) {
                AtomicReference<Expr> expr = new AtomicReference<>(expr(g.out()));
                expressions.forEach((v, e) -> expr.updateAndGet(old -> old.substitute(e, new Variable(v))));
                expressions.put(g.out(), expr.get());

                if (expr.get() instanceof BinaryExpr binary) {
                    expressions.put(g.out() + "-AND", binary.asAnd());
                }
            }

            return new Program(expressions);
        }
    }

    private record Gate(Op op, String a, String b, String out) {
        public static Gate parse(String line) {
            String[] parts = line.split(" ");
            String op = parts[1];
            return new Gate(Op.valueOf(op), parts[0], parts[2], parts[4]);
        }

        boolean isCircular(Map<String, Gate> gates) {
            return isCircular(gates, new HashSet<>());
        }

        private boolean isCircular(Map<String, Gate> gates, Set<String> visited) {
            if (!visited.add(out)) {
                return true;
            }

            return gates.get(a) != null && gates.get(a).isCircular(gates, new HashSet<>(visited)) ||
                   gates.get(b) != null && gates.get(b).isCircular(gates, new HashSet<>(visited));
        }
    }

    private enum Op {
        AND, OR, XOR;

        public int value(int a, int b) {
            return switch (this) {
                case AND -> a == 1 && b == 1 ? 1 : 0;
                case OR -> a == 1 || b == 1 ? 1 : 0;
                case XOR -> a != b ? 1 : 0;
            };
        }
    }

    private record Input(Map<String, Integer> values) {
        static Input parse(String definition) {
            Map<String, Integer> values = new HashMap<>();

            for (String line : definition.lines().toList()) {
                String[] kv = line.split(": ");
                values.put(kv[0], Integer.parseInt(kv[1]));
            }

            return new Input(values);
        }

        @SuppressWarnings("SameParameterValue")
        static Input of(long x, long y) {
            Map<String, Integer> values = new HashMap<>();
            assign(x, values, "x");
            assign(y, values, "y");
            return new Input(values);
        }

        private static void assign(long value, Map<String, Integer> values, String prefix) {
            for (int i = 0; i < 64; i++) {
                String name = prefix + (i < 10 ? "0" + i : i);
                int bitValue = (int) (value >> i) & 1;
                values.put(name, bitValue);
            }
        }
    }

    private record Program(Map<String, Expr> expressions) {
        Expr expr(String name) {
            return expressions.get(name);
        }

        Expr inline(Expr expr) {
            if (expr instanceof Variable var) {
                Expr inlined = expressions.get(var.name());
                return inlined != null ? inline(inlined) : var;
            } else if (expr instanceof BinaryExpr bin) {
                Set<Expr> inlinedOperands = bin.operands().stream().map(this::inline).collect(Collectors.toSet());
                return new BinaryExpr(inlinedOperands, bin.op());
            }

            throw new UnsupportedOperationException();
        }

        boolean equivalent(Program that) {
            return expressions.keySet().stream().allMatch(name -> equivalent(name, that));
        }

        boolean equivalent(String name, Program that) {
            return inline(expr(name)).equals(that.inline(that.expr(name)));
        }
    }

    private interface Expr {
        default Expr substitute(Expr subExpr, Expr replacement) {
            if (this.equals(subExpr)) {
                return replacement;
            } else if (this instanceof BinaryExpr bin) {
                Set<Expr> substOps = new HashSet<>();
                boolean changed = false;

                for (Expr e : bin.operands()) {
                    Expr subst = e.substitute(subExpr, replacement);
                    substOps.add(subst);

                    if (subst != e) {
                        changed = true;
                    }
                }

                if (changed) {
                    return new BinaryExpr(substOps, bin.op);
                } else {
                    return this;
                }
            } else {
                return this;
            }
        }
    }

    private record BinaryExpr(Set<Expr> operands, Op op) implements Expr {
        Expr asAnd() {
            return new BinaryExpr(operands, Op.AND);
        }
    }

    private record Variable(String name) implements Expr {}

    private record Pair(String o1, String o2) {
        boolean overlaps(Pair p2) {
            return stream().anyMatch(o -> p2.stream().anyMatch(o::equals));
        }

        Stream<String> stream() {
            return Stream.of(o1, o2);
        }

        static List<Set<Pair>> combinations(List<Pair> pairs) {
            List<Set<Pair>> combinations = new ArrayList<>();

            for (Pair p1 : pairs) {
                for (Pair p2 : pairs) {
                    if (p1.overlaps(p2)) {
                        continue;
                    }

                    for (Pair p3 : pairs) {
                        if (p1.overlaps(p3) || p2.overlaps(p3)) {
                            continue;
                        }

                        for (Pair p4 : pairs) {
                            if (p1.overlaps(p4) || p2.overlaps(p4) || p3.overlaps(p4)) {
                                continue;
                            }

                            combinations.add(Set.of(p1, p2, p3, p4));
                        }
                    }
                }
            }

            return combinations;
        }

        static List<Pair> from(Set<String> candidates) {
            List<Pair> pairs = new ArrayList<>();

            for (String c1 : candidates) {
                for (String c2 : candidates) {
                    if (c1.compareTo(c2) < 0) {
                        pairs.add(new Pair(c1, c2));
                    }
                }
            }

            return pairs;
        }
    }
}
