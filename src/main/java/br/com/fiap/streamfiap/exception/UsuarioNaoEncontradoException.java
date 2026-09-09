package br.com.fiap.streamfiap.exception;

public class UsuarioNaoEncontrado extends RuntimeException {
  public UsuarioNaoEncontrado(String message) {
    super(message);
  }
}
