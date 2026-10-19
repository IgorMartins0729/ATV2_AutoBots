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
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelo.AdicionadorLinkTelefone;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;


@RestController 
@RequestMapping("/telefone")
public class TelefoneControle {
    @Autowired 
    private TelefoneRepositorio repositorio;
    @Autowired 
    private ClienteRepositorio clienteRepositorio;
    @Autowired 
    private TelefoneAtualizador atualizador;
    @Autowired
    private AdicionadorLinkTelefone adicionadorLink;

    @GetMapping("/{id}")
    public ResponseEntity<Telefone> obterTelefone(@PathVariable long id) {
        Optional<Telefone> telefone = repositorio.findById(id);
        if (telefone.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(telefone.get());
        return new ResponseEntity<>(telefone.get(), HttpStatus.OK);
    }

    @GetMapping("/telefones")
    public ResponseEntity<List<Telefone>> obterTelefones() {
        List<Telefone> telefones = repositorio.findAll();
        adicionadorLink.adicionarLink(telefones);
        return new ResponseEntity<>(telefones, HttpStatus.OK);
    }
    
    @PostMapping("/cadastro/{clienteId}")
      public ResponseEntity<Telefone> cadastrarTelefone(@PathVariable long clienteId, @RequestBody Telefone telefone) {
              Optional<Cliente> encontrado = clienteRepositorio.findById(clienteId);
              if (encontrado.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Cliente cliente = encontrado.get();
              telefone.setId(null);
              Telefone salvo = repositorio.save(telefone);
              cliente.getTelefones().add(salvo);
              clienteRepositorio.save(cliente);
              adicionadorLink.adicionarLink(salvo);
              return new ResponseEntity<>(salvo, HttpStatus.CREATED);
      }

      @PutMapping("/atualizar/{id}")
      public ResponseEntity<Telefone> atualizarTelefone(@PathVariable long id, @RequestBody Telefone atualizacao) {
              Optional<Telefone> encontrado = repositorio.findById(id);
              if (encontrado.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Telefone telefone = encontrado.get();
              atualizador.atualizar(telefone, atualizacao);
              repositorio.save(telefone);
              adicionadorLink.adicionarLink(telefone);
              return new ResponseEntity<>(telefone, HttpStatus.OK);
      }

      @DeleteMapping("/excluir/{id}")
      public ResponseEntity<Void> excluirTelefone(@PathVariable long id) {
              Optional<Cliente> dono = clienteRepositorio.findByTelefonesId(id);
              if (dono.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Cliente cliente = dono.get();
              cliente.getTelefones().removeIf(telefone -> telefone.getId().equals(id));
              clienteRepositorio.save(cliente);
              return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
}
