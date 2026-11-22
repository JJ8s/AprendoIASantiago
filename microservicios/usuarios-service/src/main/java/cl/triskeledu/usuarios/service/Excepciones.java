package cl.triskeledu.usuarios.service;

public final class Excepciones {
    private Excepciones() {}

    public static class NoEncontrado extends RuntimeException {
        public NoEncontrado(String mensaje) { super(mensaje); }
    }

    public static class Conflicto extends RuntimeException {
        public Conflicto(String mensaje) { super(mensaje); }
    }

    public static class NoAutorizado extends RuntimeException {
        public NoAutorizado(String mensaje) { super(mensaje); }
    }
}
