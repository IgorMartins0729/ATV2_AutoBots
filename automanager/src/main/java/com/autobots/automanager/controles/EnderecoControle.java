package com.autobots.automanager.controles;

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
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelo.AdicionadorLinkEndereco;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@RestController 
@RequestMapping("/endereco")
public class EnderecoControle {
    @Autowired
    private EnderecoRepositorio repositorio;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private EnderecoAtualizador atualizador;
    @Autowired
    private AdicionadorLinkEndereco adicionadorLink;
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

      @GetMapping("/{id}")
      public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
              Optional<Endereco> endereco = repositorio.findById(id);
              if (endereco.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              adicionadorLink.adicionarLink(endereco.get());
              return new ResponseEntity<>(endereco.get(), HttpStatus.OK);
      }

      @GetMapping("/enderecos")
      public ResponseEntity<List<Endereco>> obterEnderecos() {
              List<Endereco> enderecos = repositorio.findAll();
              adicionadorLink.adicionarLink(enderecos);
              return new ResponseEntity<>(enderecos, HttpStatus.OK);
      }

      @PostMapping("/cadastro/{clienteId}")
      public ResponseEntity<Endereco> cadastrarEndereco(@PathVariable long clienteId, @RequestBody Endereco endereco) {
              Optional<Cliente> encontrado = clienteRepositorio.findById(clienteId);
              if (encontrado.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Cliente cliente = encontrado.get();
              if (cliente.getEndereco() != null) {
                      return new ResponseEntity<>(HttpStatus.CONFLICT);
              }
              if (verificador.verificar(endereco.getCidade()) || verificador.verificar(endereco.getRua())
                              || verificador.verificar(endereco.getNumero())) {
                      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
              }
              endereco.setId(null);
              Endereco salvo = repositorio.save(endereco);
              cliente.setEndereco(salvo);
              clienteRepositorio.save(cliente);
              adicionadorLink.adicionarLink(salvo);
              return new ResponseEntity<>(salvo, HttpStatus.CREATED);
      }

      @PutMapping("/atualizar/{id}")
      public ResponseEntity<Endereco> atualizarEndereco(@PathVariable long id, @RequestBody Endereco atualizacao) {
              Optional<Endereco> encontrado = repositorio.findById(id);
              if (encontrado.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Endereco endereco = encontrado.get();
              atualizador.atualizar(endereco, atualizacao);
              repositorio.save(endereco);
              adicionadorLink.adicionarLink(endereco);
              return new ResponseEntity<>(endereco, HttpStatus.OK);
      }

      @DeleteMapping("/excluir/{id}")
      public ResponseEntity<Void> excluirEndereco(@PathVariable long id) {
              Optional<Cliente> dono = clienteRepositorio.findByEnderecoId(id);
              if (dono.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Cliente cliente = dono.get();
              cliente.setEndereco(null);
              clienteRepositorio.save(cliente);
              return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
}
