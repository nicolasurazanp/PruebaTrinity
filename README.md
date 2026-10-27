# Banca — Prueba técnica Trinity

API REST para que los trabajadores de una entidad financiera administren **clientes**, sus **productos financieros** (cuentas de ahorros y corrientes) y los **movimientos transaccionales** (consignaciones, retiros y transferencias), con consulta del **estado de cuenta**.

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.1 (Spring Web MVC, Spring Data JPA, Bean Validation) |
| Hibernate | 7 |
| PostgreSQL | 16 |
| Maven | Maven Wrapper incluido |
| Pruebas | JUnit 5, Mockito, MockMvc |
| Contenedores | Docker y Docker Compose |

---

## Cómo ejecutarlo

### Opción 1: todo en Docker (recomendada)
```bash
docker compose up -d --build
```
Levanta PostgreSQL y la aplicación. La API queda en `http://localhost:8080`. La aplicación espera a que la base de datos esté lista (`healthcheck`) antes de arrancar.

```bash
docker compose down
```
Detiene los contenedores. Los datos se conservan en el volumen `postgres_data`.

### Opción 2: aplicación local y base de datos en Docker
```bash
docker compose up -d postgres
```
```bash
./mvnw spring-boot:run
```
En Windows se usa `mvnw.cmd`. La aplicación se conecta a PostgreSQL en `localhost:5433`.

### Pruebas
```bash
./mvnw test
```
**72 pruebas.** Las de integración (`ClienteFechasTest`, `TransferenciaIntegracionTest` y `BancaApplicationTests`) necesitan PostgreSQL encendido. Las unitarias de Service y Controller no.

---

## API

Todas las respuestas son JSON. Los errores siguen el estándar **RFC 9457 (Problem Details)**.

### Clientes — `/api/clientes`
| Método | URL | Descripción | Respuesta |
|---|---|---|---|
| POST | `/api/clientes` | Crear cliente | 201 + `Location` |
| GET | `/api/clientes` | Listar clientes | 200 |
| GET | `/api/clientes/{id}` | Consultar un cliente | 200 |
| PUT | `/api/clientes/{id}` | Modificar un cliente | 200 |
| DELETE | `/api/clientes/{id}` | Eliminar un cliente | 204 |

### Cuentas — `/api/cuentas`
| Método | URL | Descripción | Respuesta |
|---|---|---|---|
| POST | `/api/cuentas` | Crear cuenta de ahorros o corriente | 201 + `Location` |
| GET | `/api/cuentas/{id}` | Consultar una cuenta | 200 |
| GET | `/api/cuentas?clienteId={id}` | Cuentas de un cliente | 200 |
| PATCH | `/api/cuentas/{id}/activar` | Activar | 200 |
| PATCH | `/api/cuentas/{id}/inactivar` | Inactivar | 200 |
| PATCH | `/api/cuentas/{id}/cancelar` | Cancelar (solo con saldo $0) | 200 |

### Transacciones
| Método | URL | Descripción | Respuesta |
|---|---|---|---|
| POST | `/api/transacciones/consignaciones` | Consignar en una cuenta | 201 |
| POST | `/api/transacciones/retiros` | Retirar de una cuenta | 201 |
| POST | `/api/transacciones/transferencias` | Transferir entre cuentas | 201 (débito y crédito) |
| GET | `/api/cuentas/{id}/estado-cuenta` | Estado de cuenta con saldo y movimientos | 200 |

### Códigos de error
| Código | Cuándo |
|---|---|
| 400 Bad Request | Datos inválidos (campo obligatorio ausente, correo sin formato, monto negativo…) o JSON mal formado. Incluye `errores` con cada campo. |
| 404 Not Found | El cliente o la cuenta no existen. |
| 422 Unprocessable Content | Se incumple una regla de negocio: cliente menor de edad, identificación duplicada, cliente con productos, saldo insuficiente, cuenta no activa… |

### Ejemplo
```bash
curl -X POST http://localhost:8080/api/clientes -H "Content-Type: application/json" -d '{"tipoIdentificacion":"CC","numeroIdentificacion":"1020304050","nombres":"Ana","apellido":"Perez","correoElectronico":"ana@correo.com","fechaNacimiento":"1995-05-20"}'
```
```bash
curl -X POST http://localhost:8080/api/cuentas -H "Content-Type: application/json" -d '{"clienteId":1,"tipoCuenta":"AHORROS","exentaGmf":false}'
```
```bash
curl -X POST http://localhost:8080/api/transacciones/consignaciones -H "Content-Type: application/json" -d '{"cuentaId":1,"monto":100000.00}'
```

---

## Reglas de negocio

### Exigidas por la prueba
| Regla | Dónde se implementa |
|---|---|
| Un cliente no puede ser creado ni existir si es menor de edad | `@MayorDeEdad` (DTO y entidad) + `ClienteService` |
| Fecha de creación y de modificación automáticas | `@PrePersist` / `@PreUpdate` en las entidades |
| Un cliente no puede eliminarse si tiene productos | `ClienteService.eliminar` + clave foránea `fk_cuentas_cliente` |
| Correo con formato válido y nombre/apellido de al menos 2 caracteres | `@Email` y `@Size` |
| Solo cuentas de ahorros o corrientes | `enum TipoCuenta` + `CHECK` en la BD |
| La cuenta solo existe vinculada a un cliente | `@ManyToOne` obligatorio + clave foránea |
| Número de cuenta único, automático, de 10 dígitos, con prefijo 53 (ahorros) o 33 (corriente) | `GeneradorNumeroCuenta` + `@Pattern` + `UNIQUE` |
| La cuenta de ahorros nace activa | Constructor de `Cuenta` |
| Activar / inactivar en cualquier momento | `Cuenta.activar()` / `Cuenta.inactivar()` |
| Cancelar solo con saldo $0 | `Cuenta.cancelar()` |
| Ahorros no puede tener saldo menor a $0 | `Cuenta.debitar()` |
| Solo consignación, retiro y transferencia | `enum TipoTransaccion` + `CHECK` |
| Transferencia: débito en origen y crédito en destino | `TransaccionService.transferir` |
| El saldo se actualiza con cada transacción exitosa | `Cuenta.acreditar()` / `Cuenta.debitar()` |

### Decisiones propias (el documento no las especifica)
| Decisión | Motivo |
|---|---|
| Tipos de identificación: CC, CE y pasaporte | Sin TI (la usan menores de edad) ni NIT (son personas naturales). |
| Mayoría de edad = 18 años | Legislación colombiana. |
| No se repite tipo + número de identificación | Evita clientes duplicados. |
| La cuenta corriente también nace activa | No tiene sentido abrir una cuenta inutilizable. |
| **Ninguna** cuenta puede quedar en negativo | El documento no define cupo de sobregiro para la corriente. Bastaría cambiar `Cuenta.debitar()` si se definiera. |
| Saldo disponible = saldo | No hay bloqueos de fondos ni sobregiro. Se devuelven ambos en el estado de cuenta. |
| Solo operan las cuentas activas | Si no, inactivar no tendría efecto. |
| Las cuentas no se eliminan: se cancelan | Se conserva el historial. |
| Los movimientos son inmutables | Un error se corrige con otro movimiento. |
| Montos mayores que 0 y con máximo 2 decimales | Coinciden con `numeric(19,2)`. |
| El GMF solo se registra, no se cobra | El documento no indica cómo calcularlo. |

---

## Arquitectura

**Arquitectura en capas (MVC)**, una de las dos opciones que admite la prueba. Se eligió frente a la hexagonal porque el dominio es pequeño y las capas exigidas (*entity, service, controller, repository*) encajan directamente; la hexagonal añadiría puertos y adaptadores sin un beneficio claro a este tamaño.

```
HTTP ──▶ controller ──▶ service ──▶ repository ──▶ PostgreSQL
            │  ▲           │
            ▼  │           ▼
           dto            entity (reglas de su propio estado)
```

| Paquete | Responsabilidad |
|---|---|
| `controller` | HTTP: URLs, códigos de respuesta, validación de entrada (`@Valid`) y conversión DTO ↔ entidad. Sin reglas de negocio. |
| `dto` | Contrato de la API: lo que entra (`*Request`) y lo que sale (`*Response`). |
| `service` | Reglas de negocio que coordinan varias entidades y límites transaccionales (`@Transactional`). |
| `entity` | Modelo persistente y reglas de su propio estado (por ejemplo, `Cuenta.debitar`). |
| `repository` | Acceso a datos con Spring Data JPA. |
| `exception` | Excepciones de negocio y su traducción a HTTP. |
| `validation` | Validación propia `@MayorDeEdad`. |

---

## Patrones de diseño

| Patrón | Dónde |
|---|---|
| **Repository** | `ClienteRepository`, `CuentaRepository`, `MovimientoRepository`: aíslan el acceso a datos. |
| **Proxy** | Spring Data genera en tiempo de ejecución la implementación de los repositories; Hibernate usa proxies para la carga `LAZY`. |
| **DTO** | `ClienteRequest`, `CuentaResponse`, `EstadoCuentaResponse`…: evitan la asignación masiva y desacoplan la API de la base de datos. |
| **Static Factory Method** | `ClienteResponse.desde(...)`, `CuentaResponse.desde(...)`, `ProblemDetail.forStatusAndDetail(...)`. |
| **Inyección de dependencias** | Todas las dependencias se reciben por constructor. |
| **Modelo de dominio rico** | `Cuenta` protege sus reglas: no tiene `setSaldo` ni `setEstado`, solo `acreditar`, `debitar`, `activar`, `inactivar` y `cancelar`. |
| **Máquina de estados** | ACTIVA ⇄ INACTIVA → CANCELADA (estado final). |
| **Manejador global de excepciones** | `ManejadorGlobalExcepciones` (`@RestControllerAdvice`) centraliza la traducción de errores. |

---

## Principios SOLID

| Principio | Aplicación |
|---|---|
| **S** — Responsabilidad única | Cada capa tiene un solo motivo para cambiar. `GeneradorNumeroCuenta` solo genera números; `MayorDeEdadValidator` solo valida la edad. |
| **O** — Abierto/cerrado | Nuevas reglas de error se añaden con otro `@ExceptionHandler` sin tocar los controllers. Un cupo de sobregiro se añadiría solo en `Cuenta.debitar`. |
| **L** — Sustitución de Liskov | Los services dependen de interfaces de repository; Spring o Mockito pueden sustituirlas sin cambiar el comportamiento esperado (así se hacen los tests unitarios). |
| **I** — Segregación de interfaces | Cada repository expone solo las consultas que su agregado necesita. |
| **D** — Inversión de dependencias | Los services dependen de abstracciones (`ClienteRepository`) y las reciben por constructor; no crean sus dependencias. |

---

## ACID

| Propiedad | Cómo se garantiza |
|---|---|
| **Atomicidad** | Cada operación es un método `@Transactional`. En una transferencia, si el crédito falla después del débito, se hace rollback de todo. Las excepciones de negocio extienden `RuntimeException`, por lo que el rollback es automático. Lo verifica `TransferenciaIntegracionTest` contra PostgreSQL. |
| **Consistencia** | Validaciones en tres niveles: Bean Validation en los DTO, reglas en servicios y entidades, y restricciones en la BD (`NOT NULL`, `CHECK`, `UNIQUE`, claves foráneas). |
| **Aislamiento** | Bloqueo optimista con `@Version` en `Cuenta`: si dos operaciones modifican el mismo saldo a la vez, la segunda falla en lugar de sobrescribirlo. |
| **Durabilidad** | PostgreSQL confirma cada transacción en disco; en Docker los datos persisten en el volumen `postgres_data`. |

---

## Base de datos: DDL y DML

- **DDL**: [`docs/sql/schema.sql`](docs/sql/schema.sql) — tablas `clientes`, `cuentas` y `movimientos`, con claves primarias, claves foráneas, restricciones `UNIQUE` y `CHECK`, y el índice `idx_movimientos_cuenta_fecha` para el estado de cuenta.
- **DML**: [`docs/sql/ejemplos-dml.sql`](docs/sql/ejemplos-dml.sql) — `INSERT`, `UPDATE` y `SELECT` equivalentes a las operaciones de la API.

En desarrollo, Hibernate genera el esquema con `spring.jpa.hibernate.ddl-auto=update`. En producción se recomienda `validate` junto con migraciones versionadas (Flyway o Liquibase).

```
clientes 1 ──── N cuentas 1 ──── N movimientos
```

- El dinero se guarda como `numeric(19,2)` y en Java como `BigDecimal`, nunca `double`.
- Enums guardados como texto (`EnumType.STRING`), no por posición.

---

## Servicios en la nube

El proyecto **no usa todavía servicios en la nube**: se ejecuta localmente con Docker. Al estar contenedorizado, el despliegue propuesto en AWS sería:

| Necesidad | Servicio |
|---|---|
| Registro de la imagen Docker | Amazon ECR |
| Ejecución de la aplicación | Amazon ECS con Fargate (o AWS App Runner) |
| Base de datos | Amazon RDS for PostgreSQL |
| Credenciales | AWS Secrets Manager, inyectadas como variables de entorno |
| Logs y métricas | Amazon CloudWatch |
| Integración y despliegue continuo | GitHub Actions: `mvnw test` → construir imagen → publicar en ECR → desplegar |

La aplicación ya admite la configuración por variables de entorno (por ejemplo, `SPRING_DATASOURCE_URL`), que es lo que necesitan estos servicios.

---

## Control de versiones: Git Flow

| Rama | Uso |
|---|---|
| `main` | Versiones estables entregables. |
| `develop` | Integración de funcionalidades terminadas. |
| `feature/*` | Una rama por funcionalidad: `entidad-cliente`, `crud-cliente`, `productos`, `transacciones`, `docker`, `documentacion`. |

- Cada funcionalidad se integra en `develop` con `merge --no-ff`, para conservar su historial agrupado.
- Mensajes de commit con **Conventional Commits**: `feat`, `fix`, `test`, `refactor`, `style`, `build`, `docs`.
- Cada commit compila y pasa las pruebas.

---

## Mejoras futuras

- Mover las credenciales de la base de datos a variables de entorno o a un gestor de secretos.
- Migraciones con Flyway y `ddl-auto=validate`.
- Testcontainers para que las pruebas de integración usen una base de datos aislada.
- Paginación en los listados.
- Autenticación y autorización (Spring Security) para los trabajadores del banco.
- Documentación interactiva con OpenAPI/Swagger.
