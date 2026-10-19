package com.autobots.automanager.controles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.modelo.AdicionadorLinkCliente;
import com.autobots.automanager.modelo.ClienteAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@RestController
@RequestMapping("/cliente")
public class ClienteControle {
	@Autowired
	private ClienteRepositorio repositorio;
	@Autowired
	private ClienteAtualizador atualizador;
	@Autowired
	private AdicionadorLinkCliente adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Cliente> obterCliente(@PathVariable long id){
		Optional<Cliente> cliente = repositorio.findById(id);
		if (cliente.isEmpty()){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(cliente.get());
		return new ResponseEntity<>(cliente.get(),HttpStatus.OK);
	}

	@GetMapping("/clientes")
	public ResponseEntity<List<Cliente>> obterClientes() {
		List<Cliente> clientes = repositorio.findAll();
		adicionadorLink.adicionarLink(clientes);
		return new ResponseEntity<>(clientes, HttpStatus.OK);
	}

	@PostMapping("/cadastro")
	public ResponseEntity<Cliente> cadastrarCliente(@RequestBody Cliente cliente) {
		// ids vindos no corpo sao ignorados: cadastro sempre cria registros novos
		cliente.setId(null);
		if (cliente.getTelefones() == null) {
			cliente.setTelefones(new ArrayList<>());
		}
		if (cliente.getDocumentos() == null) {
			cliente.setDocumentos(new ArrayList<>());
		}
		cliente.getTelefones().forEach(telefone -> telefone.setId(null));
		cliente.getDocumentos().forEach(documento -> documento.setId(null));
		if (cliente.getEndereco() != null) {
			cliente.getEndereco().setId(null);
		}
		Cliente salvo = repositorio.save(cliente);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Cliente> atualizarCliente(@PathVariable long id, @RequestBody Cliente atualizacao) {
		Optional<Cliente> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Cliente cliente = encontrado.get();
		atualizador.atualizar(cliente,atualizacao);
		repositorio.save(cliente);
		adicionadorLink.adicionarLink(cliente);
		return new ResponseEntity<>(cliente, HttpStatus.OK);
	}

	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<Void> excluirCliente(@PathVariable long id) {
		if (!repositorio.existsById(id)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		repositorio.deleteById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
