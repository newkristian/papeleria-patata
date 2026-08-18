package com.kristianconk.api_papeleria.cliente;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {
    
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> getAllClientes() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ClienteResponseDTO getClienteById(Long id) {
        return clienteRepository.findById(id)
                .map(clienteMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }
    
    @Transactional
    public ClienteResponseDTO createCliente(ClienteRequestDTO request) {
        log.info("[ClienteService] - createCliente: request={}", request);
        
        if (clienteRepository.findByTelefono(request.telefono()).isPresent()) {
            throw new RuntimeException("Ya existe un cliente con el teléfono: " + request.telefono());
        }
        
        Cliente cliente = clienteMapper.toEntity(request);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toDto(cliente);
    }
    
    @Transactional
    public ClienteResponseDTO updateCliente(Long id, ClienteRequestDTO request) {
        log.info("[ClienteService] - updateCliente: id={}, request={}", id, request);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
                
        if (request.telefono() != null && !cliente.getTelefono().equals(request.telefono()) && 
            clienteRepository.findByTelefono(request.telefono()).isPresent()) {
             throw new RuntimeException("Ya existe otro cliente con el teléfono: " + request.telefono());
        }
        
        clienteMapper.updateEntity(cliente, request);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toDto(cliente);
    }
    
    @Transactional
    public void deleteCliente(Long id) {
        log.info("[ClienteService] - deleteCliente: id={}", id);
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
        if (id == 1L) {
            throw new RuntimeException("No se puede eliminar el cliente anónimo por defecto");
        }
        clienteRepository.deleteById(id);
    }
}
