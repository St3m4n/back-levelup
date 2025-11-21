package com.levelup.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.levelup.backend.dto.cart.CartDto;
import com.levelup.backend.dto.cart.CartItemRequest;
import com.levelup.backend.dto.cart.UpdateCartRequest;
import com.levelup.backend.model.Categoria;
import com.levelup.backend.model.Producto;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.model.UsuarioPerfil;
import com.levelup.backend.repository.CategoriaRepository;
import com.levelup.backend.repository.ProductoRepository;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.security.LevelUpUserDetails;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EntityManager entityManager;

    private Usuario usuario;

    private Producto producto;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.save(Categoria.builder()
                .nombre("Test Category")
                .build());

        producto = productoRepository.save(Producto.builder()
                .codigo("SKU-CART-TEST")
                .nombre("Control Test")
                .categoria(categoria)
                .precio(BigDecimal.valueOf(29990))
                .stock(10)
                .stockCritico(1)
                .build());

        usuario = usuarioRepository.save(Usuario.builder()
                .run("TEST-RUN-1")
                .nombre("Test")
                .apellidos("User")
                .correo("test-cart@example.com")
                .perfil(UsuarioPerfil.Cliente)
                .descuentoVitalicio(false)
                .systemAccount(false)
                .build());

        LevelUpUserDetails principal = new LevelUpUserDetails(usuario);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void replaceCartPersistsAndCanBeRetrievedAfterNewSession() {
        UpdateCartRequest request = new UpdateCartRequest();
        CartItemRequest itemRequest = new CartItemRequest();
        itemRequest.setProductCode(producto.getCodigo());
        itemRequest.setQuantity(3);
        request.setItems(List.of(itemRequest));

        CartDto saved = cartService.replaceCart(request);
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(saved.getTotalQuantity()).isEqualTo(3);

        // Simulate fresh session by clearing persistence context
        entityManager.flush();
        entityManager.clear();

        CartDto retrieved = cartService.getMyCart();
        assertThat(retrieved.getItems()).hasSize(1);
        assertThat(retrieved.getItems().get(0).getProductCode()).isEqualTo("SKU-CART-TEST");
        assertThat(retrieved.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(retrieved.getTotalQuantity()).isEqualTo(3);
    }

    @Test
    void emptyReplaceWithoutForceKeepsItems() {
        UpdateCartRequest request = new UpdateCartRequest();
        CartItemRequest itemRequest = new CartItemRequest();
        itemRequest.setProductCode(producto.getCodigo());
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));
        cartService.replaceCart(request);

        UpdateCartRequest emptyRequest = new UpdateCartRequest();
        emptyRequest.setItems(List.of());

        CartDto response = cartService.replaceCart(emptyRequest);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(response.getTotalQuantity()).isEqualTo(2);
    }

    @Test
    void emptyReplaceWithForceClearsItems() {
        UpdateCartRequest request = new UpdateCartRequest();
        CartItemRequest itemRequest = new CartItemRequest();
        itemRequest.setProductCode(producto.getCodigo());
        itemRequest.setQuantity(1);
        request.setItems(List.of(itemRequest));
        cartService.replaceCart(request);

        UpdateCartRequest clearRequest = new UpdateCartRequest();
        clearRequest.setItems(List.of());
        clearRequest.setForceReplace(true);

        CartDto cleared = cartService.replaceCart(clearRequest);
        assertThat(cleared.getItems()).isEmpty();
        assertThat(cleared.getTotalQuantity()).isZero();
    }
}
