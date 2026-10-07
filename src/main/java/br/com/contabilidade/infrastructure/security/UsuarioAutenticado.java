package br.com.contabilidade.infrastructure.security;

import java.util.List;
import java.util.UUID;

public record UsuarioAutenticado(UUID id, String username, List<String> papeis) {}
