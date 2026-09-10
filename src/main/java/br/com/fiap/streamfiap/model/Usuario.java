package br.com.fiap.streamfiap.model;

import br.com.fiap.streamfiap.exception.ClassificacaoIndicativaException;
import br.com.fiap.streamfiap.exception.CreditosInsuficientesException;
import br.com.fiap.streamfiap.exception.ConteudoIndisponivelException;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private int idade;
    private double creditos;

    public Usuario() {
    }

    public Usuario(String nome, int idade, double creditos) {
        this.nome = nome;
        this.idade = idade;
        this.creditos = creditos;
    }

    public boolean temCreditosSuficientes(double preco) {
        return this.creditos >= preco;
    }

    public void debitarCreditos(double valor) {
        // subtrai do valor dos créditos do usuário
        this.creditos = this.creditos - valor;
    }

    public Usuario alugar(Conteudo c) throws ClassificacaoIndicativaException {
        if (!c.isDisponivel()) {
            throw new ConteudoIndisponivelException(c.getTitulo() + " nao esta disponivel para aluguel");
        }
        if (this.idade < c.getClassificacaoEtaria()) {
            throw new ClassificacaoIndicativaException("Usuário de " + this.idade
                    + " anos não pode assistir a " + c.getTitulo()
                    + " (classificação " + c.getClassificacaoEtaria() + " anos)");
        }

        double preco = c.calcularPrecoAluguel();

        if (!temCreditosSuficientes(preco)) {
            throw new CreditosInsuficientesException("Créditos insuficientes para alugar " + c.getTitulo());
        }

        debitarCreditos(preco);
        c.setDisponivel(false);

        System.out.println("==================================================");
        System.out.println("RECIBO STREAMFIAP");
        System.out.println("Usuario: " + this.nome);
        System.out.println("Conteudo: " + c.getTitulo());
        System.out.println("Valor pago: R$ " + preco);
        System.out.println("Creditos restantes: R$ " + this.creditos);
        System.out.println("Obrigado por usar o StreamFIAP!");