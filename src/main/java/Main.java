public class Main {
    public static void main(String[] args) {
        ViewImpl view = new ViewImpl();
        view.setCallback(new ModelImpl(view));
        view.run();
    }
}
