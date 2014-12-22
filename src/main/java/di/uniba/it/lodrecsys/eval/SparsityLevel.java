package di.uniba.it.lodrecsys.eval;

/**
 * Defines the sparsity level adopted in the evaluation
 * framework
 */
public enum SparsityLevel {
    FIVE(5);
//    TEN(10),
//    TWENTY(20),
//    THIRTY(30),
//    FIFTY(50),
//    ALL(1);

    private final int id;

    SparsityLevel(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }

    public String toString() {
        return "given_" + (id == 1 ? "all" : id);
    }
}
