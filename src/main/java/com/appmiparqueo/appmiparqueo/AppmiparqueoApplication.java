package com.appmiparqueo.appmiparqueo;

import com.appmiparqueo.appmiparqueo.model.Fidelizacion;
import com.appmiparqueo.appmiparqueo.model.Parqueadero;
import com.appmiparqueo.appmiparqueo.model.Puesto;
import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.FidelizacionRepository;
import com.appmiparqueo.appmiparqueo.repository.ParqueaderoRepository;
import com.appmiparqueo.appmiparqueo.repository.PuestoRepository;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AppmiparqueoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppmiparqueoApplication.class, args);
	}

	@Bean
	CommandLineRunner initializeParkingDatabase(UsuariosRepository usuariosRepository,
			FidelizacionRepository fidelizacionRepository, ParqueaderoRepository parqueaderoRepository,
			PuestoRepository puestoRepository, PasswordEncoder passwordEncoder,
			@Value("${app.admin.email}") String adminEmail,
			@Value("${app.demo.client-email}") String clientEmail) {
		return args -> {
			Usuarios admin = createUserIfMissing(usuariosRepository, passwordEncoder, adminEmail,
					"Administrador", "MiParqueo", "0000000000", "admin");
			Usuarios client = createUserIfMissing(usuariosRepository, passwordEncoder, clientEmail,
					"Cliente", "Demo", "0000000000", "cliente");
				initializeLoyalty(fidelizacionRepository, client);

			Parqueadero parqueadero = parqueaderoRepository.findFirstByEstadoOrderByIdAsc("activo").orElse(null);
			if (parqueadero == null) {
				parqueadero = new Parqueadero();
				parqueadero.setNombre("MiParqueo Central");
				parqueadero.setDireccion("Dirección por configurar");
				parqueadero.setCiudad("Bogotá");
				parqueadero.setLatitud(new BigDecimal("4.7110000"));
				parqueadero.setLongitud(new BigDecimal("-74.0721000"));
				parqueadero.setTotalEspacios(24);
				parqueadero.setEstado("activo");
				parqueadero = parqueaderoRepository.save(parqueadero);
			}
			if (puestoRepository.count() == 0) {
				for (int number = 1; number <= parqueadero.getTotalEspacios(); number++) {
					Puesto puesto = new Puesto();
					puesto.setParqueaderoId(parqueadero.getId());
					puesto.setCodigo(String.format("A-%02d", number));
					puesto.setTipo("general");
					puesto.setZona("Nivel 1");
					puesto.setNivel("1");
					puesto.setEstado("disponible");
					puestoRepository.save(puesto);
				}
			}
			admin.setRol(1L);
		};
	}

	private static Usuarios createUserIfMissing(UsuariosRepository repository, PasswordEncoder passwordEncoder,
			String email, String firstName, String lastName, String phone, String password) {
		Usuarios existing = repository.findByCorreo(email).orElse(null);
		if (existing != null) {
			return existing;
		}

		Usuarios user = new Usuarios();
		user.setNombre(firstName);
		user.setApellido(lastName);
		user.setCorreo(email);
		user.setTelefono(phone);
		user.setContrasenaHash(passwordEncoder.encode(password));
		user.setEstado("activo");
		return repository.save(user);
	}

	private static void initializeLoyalty(FidelizacionRepository repository, Usuarios user) {
		if (repository.findByUsuario(user).isPresent()) {
			return;
		}
		Fidelizacion loyalty = new Fidelizacion();
		loyalty.setUsuario(user);
		loyalty.setPuntosAcumulados(0);
		loyalty.setNivel("basico");
		repository.save(loyalty);
	}

}
