package com.autobots.automanager.modelo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ClienteControle;
import com.autobots.automanager.entidades.Cliente;

@Component
public class AdicionadorLinkCliente implements AdicionadorLink<Cliente> {
	@Autowired
	private AdicionadorLinkTelefone adicionadorLinkTelefone;
	@Autowired
	private AdicionadorLinkDocumento adicionadorLinkDocumento;
	@Autowired
	private AdicionadorLinkEndereco adicionadorLinkEndereco;

	@Override
	public void adicionarLink(List<Cliente> lista) {
		for (Cliente cliente : lista) {
			adicionarLinkProprio(cliente);
			adicionarLinkRelacionados(cliente);
		}
	}

	@Override
	public void adicionarLink(Cliente objeto) {
		adicionarLinkProprio(objeto);
		adicionarLinkRelacionados(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ClienteControle.class)
						.obterClientes())
				.withRel("clientes");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Cliente cliente) {
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ClienteControle.class)
						.obterCliente(cliente.getId()))
				.withSelfRel();
		cliente.add(linkProprio);
	}

	// telefones, documentos e endereco dentro do cliente recebem o link proprio (self)
	private void adicionarLinkRelacionados(Cliente cliente) {
		adicionadorLinkTelefone.adicionarLink(cliente.getTelefones());
		adicionadorLinkDocumento.adicionarLink(cliente.getDocumentos());
		if (cliente.getEndereco() != null) {
			adicionadorLinkEndereco.adicionarLink(List.of(cliente.getEndereco()));
		}
	}
}
