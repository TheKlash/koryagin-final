import java.util.ArrayList;

public interface Model {
    interface Callback {
        void onSuccess(ArrayList<Equation> results);
        void onFailure(String message, Token failedToken);
    }
}
