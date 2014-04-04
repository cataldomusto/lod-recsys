package di.uniba.it.lodrecsys.eval;

/**
 * Created by asuglia on 4/4/14.
 */
public enum NumRec {
    FIVE_REC(5),
    TEN_REC(10),
    FIFTEEN_REC(15),
    TWENTY_REC(20);

    private final int id;

    NumRec(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }
}
