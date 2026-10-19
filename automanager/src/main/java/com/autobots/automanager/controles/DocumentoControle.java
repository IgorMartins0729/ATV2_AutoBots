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
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelo.AdicionadorLinkDocumento;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@RestController
@RequestMapping("/documento")
public class DocumentoControle {
      @Autowired
      private DocumentoRepositorio repositorio;
      @Autowired
      private ClienteRepositorio clienteRepositorio;
      @Autowired
      private DocumentoAtualizador atualizador;
      @Autowired
      private AdicionadorLinkDocumento adicionadorLink;

      @GetMapping("/{id}")
      public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
              Optional<Documento> documento = repositorio.findById(id);
              if (documento.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              adicionadorLink.adicionarLink(documento.get());
              return new ResponseEntity<>(documento.get(), HttpStatus.OK);
      }

      @GetMapping("/documentos")
      public ResponseEntity<List<Documento>> obterDocumentos() {
              List<Documento> documentos = repositorio.findAll();
              adicionadorLink.adicionarLink(documentos);
              return new ResponseEntity<>(documentos, HttpStatus.OK);
      }

      @PostMapping("/cadastro/{clienteId}")
      public ResponseEntity<Documento> cadastrarDocumento(@PathVariable long clienteId, @RequestBody Documento documento) {
              Optional<Cliente> encontrado = clienteRepositorio.findById(clienteId);
              if (encontrado.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Cliente cliente = encontrado.get();
              documento.setId(null);
              Documento salvo = repositorio.save(documento);
              cliente.getDocumentos().add(salvo);
              clienteRepositorio.save(cliente);
              adicionadorLink.adicionarLink(salvo);
              return new ResponseEntity<>(salvo, HttpStatus.CREATED);
      }

      @PutMapping("/atualizar/{id}")
      public ResponseEntity<Documento> atualizarDocumento(@PathVariable long id, @RequestBody Documento atualizacao) {
              Optional<Documento> encontrado = repositorio.findById(id);
              if (encontrado.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Documento documento = encontrado.get();
              atualizador.atualizar(documento, atualizacao);
              repositorio.save(documento);
              adicionadorLink.adicionarLink(documento);
              return new ResponseEntity<>(documento, HttpStatus.OK);
      }

      @DeleteMapping("/excluir/{id}")
      public ResponseEntity<Void> excluirDocumento(@PathVariable long id) {
              Optional<Cliente> dono = clienteRepositorio.findByDocumentosId(id);
              if (dono.isEmpty()) {
                      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
              }
              Cliente cliente = dono.get();
              cliente.getDocumentos().removeIf(documento -> documento.getId().equals(id));
              clienteRepositorio.save(cliente);
              return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
}