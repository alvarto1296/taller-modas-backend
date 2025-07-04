package com.alvarto.taller_modas.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alvarto.taller_modas.dtos.CategoriaDTO;
import com.alvarto.taller_modas.error.BadRequestException;
import com.alvarto.taller_modas.error.ResourceNotFoundException;
import com.alvarto.taller_modas.models.Categoria;
import com.alvarto.taller_modas.repositories.CategoriaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    
    private final CategoriaRepository categoriaRepository;
    
    // Mapear de Entidad a DTO
    private CategoriaDTO mapToDTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setCantidadProductos(categoria.getProductos() != null ? categoria.getProductos().size() : 0);
        return dto;
    }
    
    // Mapear de DTO a Entidad
    private Categoria mapToEntity(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setId(dto.getId());
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return categoria;
    }
    
    // Obtener todas las categorías
    @Transactional(readOnly = true)
    public List<CategoriaDTO> findAll() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    // Obtener una categoría por ID
    @Transactional(readOnly = true)
    public CategoriaDTO findById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return mapToDTO(categoria);
    }
    
    // Crear una nueva categoría
    @Transactional
    public CategoriaDTO create(CategoriaDTO categoriaDTO) {
        if (categoriaDTO.getId() != null) {
            throw new BadRequestException("Una nueva categoría no puede tener ID asignado");
        }
        
        if (categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + categoriaDTO.getNombre());
        }
        
        Categoria categoria = mapToEntity(categoriaDTO);
        Categoria saved = categoriaRepository.save(categoria);
        return mapToDTO(saved);
    }
    
    // Actualizar una categoría existente
    @Transactional
    public CategoriaDTO update(Long id, CategoriaDTO categoriaDTO) {
        Categoria existingCategoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        
        // Verificar si el nombre ya existe y no es el mismo
        if (!existingCategoria.getNombre().equals(categoriaDTO.getNombre()) && 
                categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + categoriaDTO.getNombre());
        }
        
        categoriaDTO.setId(id);
        Categoria categoria = mapToEntity(categoriaDTO);
        Categoria updated = categoriaRepository.save(categoria);
        return mapToDTO(updated);
    }
    
    // Eliminar una categoría
    @Transactional
    public void delete(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        
        if (categoria.getProductos() != null && !categoria.getProductos().isEmpty()) {
            throw new BadRequestException("No se puede eliminar la categoría porque tiene productos asociados");
        }
        
        categoriaRepository.deleteById(id);
    }
}