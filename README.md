# MiParqueo

Aplicación web para administrar un parqueadero: cuentas de clientes, vehículos, espacios, reservas, movimientos, pagos y fidelización.

## Requisitos

- Java 17
- MySQL 8, por ejemplo el incluido con XAMPP
- phpMyAdmin para administrar visualmente la base de datos (opcional)

## Base de datos

La configuración local está en `src/main/resources/application.properties`:

- Base de datos: `miparqueo_db`
- Servidor: `localhost:3306`
- Usuario MySQL: `root`
- Contraseña MySQL: vacía

Antes de iniciar la aplicación, crea el esquema en phpMyAdmin. Abre la pestaña **SQL** y ejecuta el siguiente script en una base vacía. No lo ejecutes sobre una base que ya contenga estas tablas o datos. Hibernate está configurado con `ddl-auto=validate`: valida el esquema existente y no crea ni modifica tablas automáticamente.

```sql
CREATE DATABASE IF NOT EXISTS miparqueo_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE miparqueo_db;

CREATE TABLE usuarios (
  id_usuario INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombres VARCHAR(80) NOT NULL,
  apellidos VARCHAR(80) NOT NULL,
  correo VARCHAR(120) NOT NULL,
  telefono VARCHAR(15) NOT NULL,
  contrasena_hash VARCHAR(255) NOT NULL,
  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado VARCHAR(20) NOT NULL DEFAULT 'activo',
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uq_usuarios_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE parqueaderos (
  id_parqueadero INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  direccion VARCHAR(150) NOT NULL,
  ciudad VARCHAR(60) NOT NULL,
  latitud DECIMAL(10,7) NOT NULL,
  longitud DECIMAL(10,7) NOT NULL,
  total_espacios INT UNSIGNED NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'activo',
  PRIMARY KEY (id_parqueadero)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE espacios (
  id_espacio INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_parqueadero INT UNSIGNED NOT NULL,
  codigo_espacio VARCHAR(20) NOT NULL,
  tipo_espacio VARCHAR(30) NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'disponible',
  nivel VARCHAR(20) DEFAULT NULL,
  zona VARCHAR(30) DEFAULT NULL,
  PRIMARY KEY (id_espacio),
  UNIQUE KEY uq_espacios_codigo (id_parqueadero, codigo_espacio),
  KEY idx_espacios_parqueadero (id_parqueadero),
  CONSTRAINT fk_espacios_parqueaderos FOREIGN KEY (id_parqueadero)
    REFERENCES parqueaderos (id_parqueadero) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE vehiculos (
  id_vehiculo INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario INT UNSIGNED NOT NULL,
  placa VARCHAR(10) NOT NULL,
  tipo_vehiculo VARCHAR(20) NOT NULL,
  marca VARCHAR(50) NOT NULL,
  modelo VARCHAR(50) NOT NULL,
  color VARCHAR(30) NOT NULL,
  codigo_qr VARCHAR(100) DEFAULT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id_vehiculo),
  UNIQUE KEY uq_vehiculos_placa (placa),
  UNIQUE KEY uq_vehiculos_codigo_qr (codigo_qr),
  KEY idx_vehiculos_usuario (id_usuario),
  CONSTRAINT fk_vehiculos_usuarios FOREIGN KEY (id_usuario)
    REFERENCES usuarios (id_usuario) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE reservas (
  id_reserva INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario INT UNSIGNED NOT NULL,
  id_vehiculo INT UNSIGNED NOT NULL,
  id_espacio INT UNSIGNED NOT NULL,
  codigo_reserva VARCHAR(40) NOT NULL,
  fecha_reserva TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  hora_inicio DATETIME NOT NULL,
  hora_fin_estimada DATETIME NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'activa',
  PRIMARY KEY (id_reserva),
  UNIQUE KEY uq_reservas_codigo (codigo_reserva),
  KEY idx_reservas_usuario (id_usuario),
  KEY idx_reservas_vehiculo (id_vehiculo),
  KEY idx_reservas_espacio (id_espacio),
  CONSTRAINT fk_reservas_espacios FOREIGN KEY (id_espacio)
    REFERENCES espacios (id_espacio) ON UPDATE CASCADE,
  CONSTRAINT fk_reservas_usuarios FOREIGN KEY (id_usuario)
    REFERENCES usuarios (id_usuario) ON UPDATE CASCADE,
  CONSTRAINT fk_reservas_vehiculos FOREIGN KEY (id_vehiculo)
    REFERENCES vehiculos (id_vehiculo) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE ingresos_salidas (
  id_movimiento INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_reserva INT UNSIGNED DEFAULT NULL,
  id_vehiculo INT UNSIGNED NOT NULL,
  hora_ingreso DATETIME NOT NULL,
  hora_salida DATETIME DEFAULT NULL,
  metodo_acceso VARCHAR(20) NOT NULL,
  observaciones VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id_movimiento),
  KEY idx_ingresos_reserva (id_reserva),
  KEY idx_ingresos_vehiculo (id_vehiculo),
  CONSTRAINT fk_ingresos_reservas FOREIGN KEY (id_reserva)
    REFERENCES reservas (id_reserva) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_ingresos_vehiculos FOREIGN KEY (id_vehiculo)
    REFERENCES vehiculos (id_vehiculo) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE pagos (
  id_pago INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_reserva INT UNSIGNED NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  metodo_pago VARCHAR(30) NOT NULL,
  estado_pago VARCHAR(20) NOT NULL DEFAULT 'pendiente',
  fecha_pago DATETIME DEFAULT NULL,
  referencia_transaccion VARCHAR(80) DEFAULT NULL,
  PRIMARY KEY (id_pago),
  UNIQUE KEY uq_pagos_referencia (referencia_transaccion),
  KEY idx_pagos_reserva (id_reserva),
  CONSTRAINT fk_pagos_reservas FOREIGN KEY (id_reserva)
    REFERENCES reservas (id_reserva) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE ubicaciones_vehiculo (
  id_ubicacion INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_movimiento INT UNSIGNED NOT NULL,
  latitud DECIMAL(10,7) NOT NULL,
  longitud DECIMAL(10,7) NOT NULL,
  nivel VARCHAR(20) DEFAULT NULL,
  zona VARCHAR(30) DEFAULT NULL,
  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_ubicacion),
  KEY idx_ubicaciones_movimiento (id_movimiento),
  CONSTRAINT fk_ubicaciones_movimientos FOREIGN KEY (id_movimiento)
    REFERENCES ingresos_salidas (id_movimiento) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE fidelizacion (
  id_fidelizacion INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario INT UNSIGNED NOT NULL,
  puntos_acumulados INT UNSIGNED NOT NULL DEFAULT 0,
  nivel VARCHAR(30) NOT NULL DEFAULT 'basico',
  ultima_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_fidelizacion),
  UNIQUE KEY uq_fidelizacion_usuario (id_usuario),
  KEY idx_fidelizacion_usuario (id_usuario),
  CONSTRAINT fk_fidelizacion_usuarios FOREIGN KEY (id_usuario)
    REFERENCES usuarios (id_usuario) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE notificaciones (
  id_notificacion INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario INT UNSIGNED NOT NULL,
  titulo VARCHAR(100) NOT NULL,
  mensaje VARCHAR(255) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  leida TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id_notificacion),
  KEY idx_notificaciones_usuario (id_usuario),
  CONSTRAINT fk_notificaciones_usuarios FOREIGN KEY (id_usuario)
    REFERENCES usuarios (id_usuario) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

Al iniciar la aplicación, se crean las cuentas de demostración, el parqueadero y 24 espacios si aún no existen.

No uses las credenciales locales de MySQL en producción. Configura usuario y contraseña mediante variables de entorno o un archivo de configuración excluido del repositorio.

## Ejecutar

Desde la carpeta del proyecto, en PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación queda disponible en:

- Inicio de sesión: http://localhost:8081/login.html
- Panel de cliente: http://localhost:8081/view/cliente
- Panel de administración: http://localhost:8081/dashboard
- Administración de usuarios: http://localhost:8081/view/usuarios/list

Para ejecutar las pruebas:

```powershell
.\mvnw.cmd test
```

## Cuentas de demostración

Al iniciar, la aplicación crea estas cuentas si sus correos todavía no existen:

| Perfil | Usuario | Contraseña inicial |
| --- | --- | --- |
| Administrador | `admin@miparqueo.local` | `admin` |
| Cliente | `cliente@miparqueo.local` | `cliente` |

Las contraseñas se guardan como hashes BCrypt. El inicializador no reemplaza la contraseña de una cuenta existente; si ya había una cuenta con ese correo, su contraseña puede ser distinta. Cambia las claves de demostración antes de usar la aplicación fuera del entorno local.

El administrador se identifica por el correo configurado en `app.admin.email`. Las demás cuentas se tratan como clientes.

## Servicio de autenticación

La API recibe y responde JSON:

- `POST /api/auth/register`: registra una cuenta con `usuario` (correo) y `contrasena`.
- `POST /api/auth/login`: autentica y crea una sesión web.

Ejemplo de solicitud:

```json
{
  "usuario": "cliente@ejemplo.com",
  "contrasena": "mi-clave"
}
```

El registro también acepta opcionalmente `nombres`, `apellidos` y `telefono`.

## Paneles

- El cliente consulta sus vehículos, espacios disponibles, reservas, estancias, pagos y puntos.
- El administrador gestiona ocupación, ingresos y salidas, alertas, pagos, vehículos, fidelización y usuarios.
- La eliminación definitiva de una cuenta elimina sus datos asociados y requiere confirmación.
