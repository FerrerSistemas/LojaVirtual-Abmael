package com.lojavirtualabmael.resouces;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.lojavirtualabmael.domain.Categoria;
import com.lojavirtualabmael.domain.Cliente;
import com.lojavirtualabmael.dto.CategoriaDTO;
import com.lojavirtualabmael.dto.ClienteDTO;
import com.lojavirtualabmael.dto.ClienteNewDTO;
import com.lojavirtualabmael.service.ClienteService;

@RestController
@RequestMapping(value="/clientes")
public class ClienteResource {
	
	@Autowired
	private ClienteService servico;

	
		@RequestMapping(value="/{id}", method = RequestMethod.GET)
		public ResponseEntity<Cliente> find(@PathVariable Integer id) {
			Cliente cli = servico.find(id);
			return ResponseEntity.ok().body(cli);
			
		}
		
		@RequestMapping(method = RequestMethod.POST)
		public ResponseEntity<Void> insert(@Valid @RequestBody ClienteNewDTO objDTO){
			
			Cliente obj = servico.fromDTO(objDTO);
			
			obj = servico.insert(obj);
			
			URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
					.path("/{id}").buildAndExpand(obj.getId()).toUri();
			
			return ResponseEntity.created(uri).build();
			
		}
		
		
		
		@RequestMapping(value="/{id}", method = RequestMethod.PUT)
		public ResponseEntity<Cliente> update(@Valid @RequestBody ClienteDTO objDTO, @PathVariable Integer id) {
			
			Cliente obj = servico.fromDTO(objDTO); 
			obj.setId(id);
			obj = servico.update(obj);
			return ResponseEntity.noContent().build();
		}
		
		@RequestMapping(value="/{id}", method = RequestMethod.DELETE)
		public ResponseEntity<Void> delete(@PathVariable Integer id) {
			servico.delete(id);
			
			return ResponseEntity.noContent().build();
			
		}
		
		
		@RequestMapping(method = RequestMethod.GET)
		public ResponseEntity <List<ClienteDTO>> findAll() {
			List<Cliente> lista = servico.findAll();
			List<ClienteDTO> listaDTO = lista.stream()
					.map(obj -> new ClienteDTO(obj)).collect(Collectors.toList());
			return ResponseEntity.ok().body(listaDTO);
			
		}
		

		@RequestMapping(value="pages", method = RequestMethod.GET)
		public ResponseEntity <Page<ClienteDTO>> findAPage(

				@RequestParam(value="page", defaultValue="0") Integer page, 
				@RequestParam(value="linesPerPage", defaultValue="24") Integer linesPerPage,
				@RequestParam(value="orderBy", defaultValue="nome") String orderBy, 
				@RequestParam(value="direction", defaultValue="ASC") String direction) {
			
			
			Page<Cliente> lista = servico.findPage(page, linesPerPage, orderBy, direction);
			Page<ClienteDTO> listaDTO = lista.map(obj -> new ClienteDTO(obj));
			return ResponseEntity.ok().body(listaDTO);
			
		}
}

