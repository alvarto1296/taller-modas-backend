package com.alvarto.taller_modas.services;

import com.alvarto.taller_modas.dtos.ProductoDTO;
import com.alvarto.taller_modas.error.BadRequestException;
import com.alvarto.taller_modas.error.ResourceNotFoundException;
import com.alvarto.taller_modas.models.Categoria;
import com.alvarto.taller_modas.models.Producto;
import com.alvarto.taller_modas.repositories.CategoriaRepository;
import com.alvarto.taller_modas.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoDTO productoDTO;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Camisas");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Camisa de Lino");
        producto.setPrecio(new BigDecimal("50.00"));
        producto.setCategoria(categoria);

        productoDTO = new ProductoDTO();
        productoDTO.setId(1L);
        productoDTO.setNombre("Camisa de Lino");
        productoDTO.setPrecio(new BigDecimal("50.00"));
        productoDTO.setCategoriaId(1L);
    }

    @Test
    @DisplayName("findById: Debe devolver un ProductoDTO cuando el producto existe")
    void whenFindById_withExistingProduct_thenReturnProductoDTO() {
        // GIVEN
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // WHEN
        ProductoDTO found = productoService.findById(1L);

        // THEN
        assertThat(found).isNotNull();
        assertThat(found.getNombre()).isEqualTo("Camisa de Lino");
        assertThat(found.getCategoriaNombre()).isEqualTo("Camisas");
    }

    @Test
    @DisplayName("findById: Debe lanzar ResourceNotFoundException si el producto no existe")
    void whenFindById_withNonExistingProduct_thenThrowResourceNotFoundException() {
        // GIVEN
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> productoService.findById(99L));
    }

    @Test
    @DisplayName("create: Debe crear y devolver un nuevo ProductoDTO")
    void whenCreate_withValidDTO_thenReturnNewProductoDTO() {
        // GIVEN
        ProductoDTO newDto = new ProductoDTO();
        newDto.setNombre("Pantalón");
        newDto.setCategoriaId(1L);

        Producto newProducto = new Producto();
        newProducto.setNombre("Pantalón");
        newProducto.setCategoria(categoria);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(newProducto);

        // WHEN
        ProductoDTO created = productoService.create(newDto);

        // THEN
        assertThat(created).isNotNull();
        assertThat(created.getNombre()).isEqualTo("Pantalón");
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("create: Debe lanzar BadRequestException si el DTO ya tiene un ID")
    void whenCreate_withDtoWithId_thenThrowBadRequestException() {
        // GIVEN
        // productoDTO ya tiene un ID de setUp()

        // WHEN & THEN
        assertThrows(BadRequestException.class, () -> productoService.create(productoDTO));
    }
}