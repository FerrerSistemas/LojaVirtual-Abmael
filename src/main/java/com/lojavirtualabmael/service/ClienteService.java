package com.lojavirtualabmael.service;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lojavirtualabmael.domain.Cidade;
import com.lojavirtualabmael.domain.Cliente;
import com.lojavirtualabmael.domain.Endereco;
import com.lojavirtualabmael.domain.enuns.Perfil;
import com.lojavirtualabmael.domain.enuns.TipoCliente;
import com.lojavirtualabmael.dto.ClienteDTO;
import com.lojavirtualabmael.dto.ClienteNewDTO;
import com.lojavirtualabmael.repository.ClienteRepository;
import com.lojavirtualabmael.repository.EnderecoRepository;
import com.lojavirtualabmael.security.UserSS;
import com.lojavirtualabmael.service.exception.AuthorizationException;
import com.lojavirtualabmael.service.exception.DataIntegrityException;
import com.lojavirtualabmael.service.exception.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private BCryptPasswordEncoder pe;

	@Autowired
	private ClienteRepository repo;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private S3Service s3Service;

	@Autowired
	private ImageService imageService;

	@Value("${img.prefix.client.profile}")
	private String prefix;

	@Value("${img.profile.size}")
	private Integer size;

	public Cliente find(Integer id) {

		UserSS user = UserService.authenticated();
		if (user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}

		Optional<Cliente> obj = repo.findById(id);
		if (obj == null) {

		}
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}

	@Transactional
	public Cliente insert(Cliente obj) {

		obj.setId(null);

		obj = repo.save(obj);

		enderecoRepository.saveAll(obj.getEnderecos());

		return obj;
	}

	public Cliente update(Cliente obj) {

		Cliente newObj = find(obj.getId());
		updateDate(newObj, obj);
		return repo.save(newObj);
	}

	private void updateDate(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}

	public void delete(Integer id) {

		find(id);

		try {
			repo.deleteById(id);

		} catch (DataIntegrityViolationException e) {

			throw new DataIntegrityException("Não é possivel excluir um Cliente com pedidos associados");
		}

	}

	public List<Cliente> findAll() {

		return repo.findAll();
	}

	public Cliente findByEmail(String email) {
		
		UserSS user = UserService.authenticated();
		if (user == null || !user.hasRole(Perfil.ADMIN) && !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado");
		}
		Cliente obj = repo.findByEmail(email);
		if (obj == null) {
			throw new ObjectNotFoundException(
					
					"Objeto não encontrado! Id " + user.getId() + ", Tipo: " + Cliente.class.getName());
		}
		return obj;
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {

		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	public Cliente fromDTO(ClienteDTO objDTO) {
		return new Cliente(objDTO.getId(), objDTO.getNome(), objDTO.getEmail(), null, null, null);

	}

	public Cliente fromDTO(ClienteNewDTO objDTO) {

		Cliente cli = new Cliente(null, objDTO.getNome(), objDTO.getEmail(), objDTO.getCpfOuCnpj(),
				TipoCliente.toEnum(objDTO.getTipo()), pe.encode(objDTO.getSenha()));

		Cidade cid = new Cidade(objDTO.getCidadeId(), null, null);

		Endereco end = new Endereco(null, objDTO.getLogradouro(), objDTO.getNumero(), objDTO.getComplemento(),
				objDTO.getBairro(), objDTO.getCep(), cli, cid);

		cli.getEnderecos().add(end);

		cli.getTelefones().add(objDTO.getTelefone1());

		if (objDTO.getTelefone2() != null) {
			cli.getTelefones().add(objDTO.getTelefone2());
		}
		if (objDTO.getTelefone2() != null) {
			cli.getTelefones().add(objDTO.getTelefone3());
		}
		return cli;
	}

	public URI uploadProfilePincture(MultipartFile multipartFile) {

		UserSS user = UserService.authenticated();

		if (user == null) {
			throw new AuthorizationException("Acesso negado");
		}

		BufferedImage jpgImage = imageService.getJpgImageFromFile(multipartFile);

		jpgImage = imageService.cropSquare(jpgImage);

		jpgImage = imageService.resize(jpgImage, size);

		String fileName = prefix + user.getId() + "jpg";

		return s3Service.upLoadFile(imageService.getInputStream(jpgImage, "jpg"), fileName, "image");

	}

}
