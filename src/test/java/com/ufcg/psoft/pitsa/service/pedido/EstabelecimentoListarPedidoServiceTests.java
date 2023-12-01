package com.ufcg.psoft.pitsa.service.pedido;

import com.ufcg.psoft.pitsa.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.pitsa.exception.EstabelecimentoNaoExisteException;
import com.ufcg.psoft.pitsa.exception.PedidoNaoExisteException;
import com.ufcg.psoft.pitsa.model.*;
import com.ufcg.psoft.pitsa.repository.ClienteRepository;
import com.ufcg.psoft.pitsa.repository.EstabelecimentoRepository;
import com.ufcg.psoft.pitsa.repository.PedidoRepository;
import com.ufcg.psoft.pitsa.repository.SaborRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@DisplayName("Testes do Serviço de listagem de pedido por clientes")
public class EstabelecimentoListarPedidoServiceTests {
    @Autowired
    EstabelecimentoListarPedidoService driver;
    @Autowired
    PedidoRepository pedidoRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    EstabelecimentoRepository estabelecimentoRepository;
    @Autowired
    SaborRepository saborRepository;

    Cliente cliente;
    Sabor sabor1;
    Sabor sabor2;
    Pizza pizza1;
    Pizza pizza2;
    Estabelecimento estabelecimento;
    Pedido pedido;

    @BeforeEach
    void setup() {
        estabelecimento = estabelecimentoRepository.save(Estabelecimento.builder()
                .codigoAcesso("654321")
                .build());
        sabor1 = saborRepository.save(Sabor.builder()
                .nome("Sabor Um")
                .tipo("salgado")
                .precoM(10.0)
                .precoG(20.0)
                .disponivel(true)
                .build());
        sabor2 = saborRepository.save(Sabor.builder()
                .nome("Sabor Dois")
                .tipo("doce")
                .precoM(10.0)
                .precoG(30.0)
                .disponivel(true)
                .build());
        cliente = clienteRepository.save(Cliente.builder()
                .nome("Anton Ego")
                .endereco("Paris")
                .codigoAcesso("123456")
                .build());
        pizza1 = Pizza.builder()
                .sabor1(sabor1)
                .tamanho("media")
                .build();
        List<Pizza> pizzas = List.of(pizza1);
        pedido = pedidoRepository.save(Pedido.builder()
                .preco(10.0)
                .enderecoEntrega("Casa 237")
                .clienteId(cliente.getId())
                .estabelecimentoId(estabelecimento.getId())
                .pizzas(pizzas)
                .build());
    }

    @AfterEach
    void tearDown() {
        clienteRepository.deleteAll();
        estabelecimentoRepository.deleteAll();
        pedidoRepository.deleteAll();
        saborRepository.deleteAll();
    }

    @Test
    @DisplayName("Quando um estabelecimento lista todos os seus pedidos salvos")
    void quandoListamosTodosOsPedidosSalvosPrimeiro() {
        pizza2 = Pizza.builder()
                .sabor1(sabor1)
                .sabor2(sabor2)
                .tamanho("grande")
                .build();
        List<Pizza> pizzas = List.of(pizza1, pizza2);
        Pedido pedido1 = pedidoRepository.save(Pedido.builder()
                .preco(25.0)
                .enderecoEntrega("Apartamento 237")
                .clienteId(cliente.getId())
                .estabelecimentoId(estabelecimento.getId())
                .pizzas(pizzas)
                .build());

        // Act
        List<Pedido> resultado = driver.estabelecimentoListar(null, estabelecimento.getId());

        // Assert
        assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertEquals(pedido, resultado.get(0)),
                () -> assertEquals(pedido1, resultado.get(1)));
    }

    @Test
    @DisplayName("Quando um Estabelecimento lista um pedido salvo pelo id primeiro")
    void quandoListamosUmPedidoSalvoPeloIdPrimeiro() {
        // Arrange
        // nenhuma necessidade além do setup()

        // Act
        List<Pedido> resultado = driver.estabelecimentoListar(pedido.getId(), estabelecimento.getId());

        // Assert
        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(pedido, resultado.get(0))
        );
    }

    @Test
    @DisplayName("Quando um Estabelecimento lista um pedido salvo pelo id segundo ou posterior")
    void quandoListamosUmPedidoSalvoPeloIdSegundoOuPosterior() {
        // Arrange
        pizza2 = Pizza.builder()
                .sabor1(sabor1)
                .sabor2(sabor2)
                .tamanho("grande")
                .build();
        List<Pizza> pizzas = List.of(pizza1, pizza2);
        Pedido pedido1 = pedidoRepository.save(Pedido.builder()
                .preco(25.0)
                .enderecoEntrega("Apartamento 237")
                .clienteId(cliente.getId())
                .estabelecimentoId(estabelecimento.getId())
                .pizzas(pizzas)
                .build());

        // Act
        List<Pedido> resultado = driver.estabelecimentoListar(pedido1.getId(), estabelecimento.getId());

        // Assert
        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(pedido1, resultado.get(0))
        );
    }

    @Test
    @DisplayName("Quando listamos um pedido pelo id inexistente")
    void quandoListamosUmPedidoPeloIdInexistente() {
        // Arrange
        // nenhuma necessidade além do setup()

        // Act
        PedidoNaoExisteException thrown = assertThrows(
                PedidoNaoExisteException.class,
                () -> driver.estabelecimentoListar(999L, estabelecimento.getId())
        );

        // Assert
        assertEquals("O pedido consultado nao existe!", thrown.getMessage());
    }

    @Test
    @DisplayName("Quando um Estabelecimento lista um pedido passando seu id invalido")
    void quandoListamosUmPedidoPassandoIdClienteInvalido() {
        // Arrange
        // nenhuma necessidade além do setup()

        // Act
        EstabelecimentoNaoExisteException thrown = assertThrows(
                EstabelecimentoNaoExisteException.class,
                () -> driver.estabelecimentoListar(pedido.getId(), 999L)
        );

        // Assert
        assertEquals("O estabelecimento consultado nao existe!", thrown.getMessage());
    }

    @Test
    @DisplayName("Quando um Estabelecimento lista um pedido que não é dele")
    void quandoListamosUmPedidoQueNaoEhDoEstabelecimento() {
        // Arrange
        Estabelecimento estabelecimento2 = estabelecimentoRepository.save(Estabelecimento.builder()
                .codigoAcesso("654301")
                .build());

        // Act
        CodigoDeAcessoInvalidoException thrown = assertThrows(
                CodigoDeAcessoInvalidoException.class,
                () -> driver.estabelecimentoListar(pedido.getId(), estabelecimento2.getId())
        );

        // Assert
        assertEquals("Codigo de acesso invalido!", thrown.getMessage());
    }
}
