# MindSet — Desarrollo local y contratos REST

Estado al 13 de noviembre de 2026. Esta guía complementa el documento principal y la matriz originales, que se conservan sin modificaciones. Distingue la implementación local de la arquitectura futura descrita en esos documentos.

## Línea de desarrollo y reconocimiento inicial

Se retomó `features/diario` en `7ec790d`, sin cambios locales pendientes. Usuarios ya tenía registro, login, JWT con `usuarioId`, CRUD y cuenta propia. Diario tenía CRUD, propiedad, límite diario y resumen semanal. Profesionales y Citas solo tenían la aplicación inicial y dependencias Maven.

Se revisaron `Documento_MindSet.docx`, las cuatro hojas de `Requerimientos_MindSet.xlsx`, README, configuraciones, código y ramas. No se encontró material adicional del profesor en el repositorio. La documentación original menciona rutas antiguas bajo `documentacion/`; los archivos reales están en `MindSet/documentos/`.

La rama `features/profesionales` parte de los avances de Usuarios y Diario; `features/citas` incorpora Profesionales y es la línea acumulativa actual. `main`, `develop` y `features/usuarios` no contienen automáticamente estos cambios. No se realizaron merges, eliminaciones de ramas ni reescrituras de historial.

## Cobertura actual

| Requisitos | Implementación local | Verificación pendiente |
| --- | --- | --- |
| RF-01 y RF-02 | Registro público siempre ESTUDIANTE, BCrypt, login y JWT con ID y rol. Se conservó el CRUD existente. | Integración del JWT entre los cuatro servicios. |
| RF-03 | POST administrativo que fuerza PROFESIONAL, con normalización de correo y duplicados 409. | Cuenta administrativa autorizada y pruebas de permisos. |
| RF-04 a RF-06 | Diario propio exclusivo de ESTUDIANTE, un registro por día, historial y resumen con aviso no clínico. | Postman, concurrencia real y resumen con datos de distintos días. |
| RF-07 a RF-09 | Crear/actualizar perfil propio; consulta estudiantil de perfiles completos y activos. | Seguridad HTTP y persistencia real. |
| RF-10 a RF-13 | Horarios propios futuros, sin superposición; reservados inmutables. | Pruebas con dos peticiones simultáneas y propietarios distintos. |
| RF-14 y RF-16 | Solicitud PENDIENTE, una activa por estudiante y consultas propias. | Restricción única y errores HTTP sobre PostgreSQL. |
| RF-15 | Contrato de reserva atómica/idempotente y procesador REST local provisional. | Flujo integrado, recuperación tras fallos; procesamiento asíncrono diferido. |
| RF-17 y RF-18 | Consultas del profesional asignado y transición CONFIRMADA a COMPLETADA. | Pruebas de autorización y transiciones. |
| RF-19 | Contrato futuro documentado abajo. No se envían notificaciones. | Implementación en la fase posterior acordada. |

Los RNF de infraestructura, TLS, cifrado en reposo, carga, disponibilidad, monitoreo, reintentos automáticos y DLQ no se declaran cumplidos por una compilación local.

## Configuración

| Servicio | Puerto | Base de datos |
| --- | --- | --- |
| ms-usuarios | 8081 | usuarios_db |
| ms-diario | 8082 | diario_emocional_db |
| ms-profesionales | 8083 | profesionales_db |
| ms-citas | 8084 | solicitudes_cita_db |

Se conservan Java 21, Spring Boot 4.1.1, Maven Wrapper, JJWT 0.13.0 y PostgreSQL 17. No hay relaciones JPA entre bases diferentes. `Horario` pertenece a `Profesional` mediante `@ManyToOne`; `Reserva` referencia un único horario dentro de Profesionales. Los identificadores de Usuarios y Citas son referencias externas, sin claves foráneas entre servicios.

1. Utilizar un JDK 21 en `JAVA_HOME`.
2. Configurar `MindSet/.env` local (ignorado por Git), usando `.env.example` como referencia. No sobrescribir un `.env` que ya tenga valores.
3. Definir `POSTGRES_PASSWORD` y el mismo `JWT_SECRET` en los cuatro servicios. El JWT requiere Base64 de al menos 32 bytes aleatorios. La firma se genera con HS256 y expira en una hora.
4. Para el flujo local, configurar una `INTERNAL_API_KEY` aleatoria diferente, de al menos 32 caracteres, igual en Profesionales y Citas. No es una contraseña de usuario y nunca se entrega al cliente público. Vacía o demasiado corta bloquea todos los endpoints internos.
5. Mantener el Compose existente. Si es necesario iniciarlo: `docker compose up -d postgres` desde `MindSet/`. No borrar volúmenes ni bases. Las cuatro bases deben existir; el script de inicialización de Docker solo se ejecuta al crear un volumen nuevo.
6. Ejecutar cada servicio desde su directorio Maven con `.\mvnw.cmd spring-boot:run`. La importación relativa de `.env` supone ese directorio de trabajo. En IntelliJ debe configurarse igualmente o pasarse las variables por entorno.

La configuración actual usa el usuario local PostgreSQL `postgres`, heredado del proyecto. Las credenciales separadas por servicio y el despliegue seguro quedan para la fase de infraestructura. No se cambiaron contraseñas, cuentas ni datos durante este desarrollo.

### Administrador inicial

No existe registro público de administradores, cuenta fija ni inicializador oculto. Un operador con acceso autorizado a `usuarios_db` debe seleccionar y verificar una cuenta creada con su contraseña privada y asignarle `ADMINISTRADOR` de manera controlada. Debe revisar ID y correo exactos, realizar el cambio en una transacción y confirmar que afectó solo a esa cuenta. Después debe iniciar sesión para obtener un JWT nuevo. Este paso no fue ejecutado automáticamente: no se eligió una identidad administrativa en nombre del usuario.

El endpoint de alta profesional no permite elegir el rol. El registro público también ignora cualquier rol enviado por el cliente. El CRUD administrativo preexistente se conserva por instrucción del proyecto; no habilita acceso al diario de otras personas.

## Contratos públicos

Salvo registro y login, enviar `Authorization: Bearer <JWT>`. Un token ausente/inválido produce 401; un rol o propietario no permitido produce 403. Un recurso ausente produce 404, datos inválidos 400 y conflictos de negocio 409. Los campos de identidad y estado se determinan en el servidor.

### Usuarios

| Método y ruta | Acceso | Cuerpo |
| --- | --- | --- |
| POST /api/usuarios/registro | Público | correo, contrasena |
| POST /api/usuarios/login | Público | correo, contrasena |
| POST /api/usuarios/profesionales | ADMINISTRADOR | correo, contrasena |
| GET, PUT, DELETE /api/usuarios/mi-cuenta | Cuenta autenticada | PUT: correo, contrasena |
| GET /api/usuarios | ADMINISTRADOR | Sin cuerpo |
| GET, PUT, DELETE /api/usuarios/{id} | ADMINISTRADOR | PUT: correo, contrasena |

Registro y alta profesional devuelven 201 con ID, correo y rol, sin contraseña ni hash. Los correos se normalizan y tienen restricción única. BCrypt admite hasta 72 bytes UTF-8; una contraseña que excede ese límite se rechaza antes de codificarla. Usuarios comprueba el ID estable y el correo del JWT contra su base para evitar transferir identidad al reutilizar un correo. Los tokens antiguos sin `usuarioId` requieren iniciar sesión nuevamente.

### Diario

Se conserva `/api/diario/registros`: POST, GET, GET/PUT/DELETE `/{id}`, GET `/usuario/{usuarioId}` y GET `/resumen-7-dias`. Todas las operaciones requieren ESTUDIANTE y propiedad. El día de registro y los siete días inclusivos del resumen usan `America/Santiago`. El resumen incluye cantidad, promedio y aviso informativo; sin registros mantiene el promedio 0.0 y cantidad 0.

### Profesionales

| Método y ruta | Acceso | Comportamiento |
| --- | --- | --- |
| POST /api/profesionales/mi-perfil | PROFESIONAL | Crea una vez; 201. |
| GET, PUT /api/profesionales/mi-perfil | PROFESIONAL | Consulta o actualiza el perfil del ID del JWT. |
| GET /api/profesionales | ESTUDIANTE | Lista perfiles completos y activos. |
| GET /api/profesionales/{usuarioId} | ESTUDIANTE | Consulta un perfil público; incompleto/inactivo devuelve 404. |
| GET, POST /api/profesionales/mis-horarios | PROFESIONAL | Consulta propios o crea horario; POST devuelve 201. |
| PUT, DELETE /api/profesionales/mis-horarios/{id} | PROFESIONAL propietario | Solo horarios futuros no reservados; DELETE devuelve 204. |

Cuerpo de perfil: `nombre` (1–120 caracteres), `especialidad` (1–120), `descripcion` (1–1000), `activo` (booleano). Todos los textos son obligatorios. No se admite un perfil incompleto persistido; omitir `activo` lo deja inactivo. No hay actualizaciones parciales: PUT recibe el perfil completo. El ID del cuerpo no determina el propietario.

Cuerpo de horario: `inicio` y `fin` como instantes ISO-8601 con zona/UTC, por ejemplo `2030-01-10T14:00:00Z` y `2030-01-10T15:00:00Z`. Se normalizan a microsegundos, la precisión de PostgreSQL. El fin debe ser posterior al inicio y el inicio futuro. Los intervalos son semiabiertos: terminar a las 15:00 y comenzar otro horario a las 15:00 es válido. Crear disponibilidad requiere un perfil completo y activo.

Respuesta de horario: `id`, `profesionalId`, `inicio`, `fin`, `reservado`. No expone estudiante ni ID de solicitud. Desactivar un perfil lo excluye de nuevas asignaciones y de la lista pública; no cancela citas existentes.

### Citas

| Método y ruta | Acceso | Comportamiento |
| --- | --- | --- |
| POST /api/citas/solicitudes | ESTUDIANTE | Crea PENDIENTE; devuelve 201. |
| GET /api/citas/solicitudes | ESTUDIANTE | Historial propio. |
| GET /api/citas/solicitudes/{id} | ESTUDIANTE propietario | Estado y datos propios. |
| GET /api/citas/asignadas | PROFESIONAL | Solo citas asignadas a su ID de usuario. |
| PUT /api/citas/asignadas/{id}/completar | PROFESIONAL asignado | CONFIRMADA → COMPLETADA, después del fin del horario. |

La solicitud acepta **una** de estas opciones:

```json
{"fecha":"2030-01-10"}
```

```json
{"desde":"2030-01-10T14:00:00Z","hasta":"2030-01-10T18:00:00Z"}
```

`fecha` representa el día en `America/Santiago`; admite hoy y días posteriores, pero solo se reservan horas futuras. No se permite combinar fecha con rango. Un rango explícito debe comenzar en el futuro. Se busca un horario íntegramente contenido en la preferencia. No se inventa una duración ni una preferencia de especialidad que la matriz no define. La selección local recorre profesionales por ID y luego sus horarios por inicio.

Transiciones: PENDIENTE → CONFIRMADA o RECHAZADA; CONFIRMADA → COMPLETADA. No hay endpoint de cambio libre de estado ni cancelación. La columna única nullable `estudianteActivo` conserva el ID del estudiante mientras la solicitud está PENDIENTE o CONFIRMADA y se libera al rechazar o completar. Esto respalda la validación previa incluso si dos POST llegan simultáneamente. `horarioId` también es único en Citas.

## Reserva y procesamiento local provisional

Estos contratos solo aceptan `X-MindSet-Internal-Key`, con la clave técnica configurada. Los JWT de estudiantes, profesionales y administradores no conceden permisos de sistema.

### Reserva en Profesionales

`POST /interno/reservas/{solicitudId}` con `desde` y `hasta` en el cuerpo. Devuelve 200:

```json
{"solicitudId":1,"horarioId":12,"profesionalId":9,"inicio":"2030-01-10T14:00:00Z","fin":"2030-01-10T15:00:00Z"}
```

Si no existe disponibilidad, los cuatro campos de asignación son null. Ese resultado también se persiste. Repetir la misma solicitud y preferencias devuelve el resultado original incluso si el perfil se desactiva o pasa la fecha. Reutilizar la clave con preferencias distintas produce 409. No hay liberación de reservas porque las cancelaciones están fuera del alcance.

Una fila de reserva por solicitud y restricciones únicas impiden resultados duplicados. Las escrituras de horarios y las reservas bloquean la misma fila del profesional con `PESSIMISTIC_WRITE`; así se serializan las operaciones de su agenda dentro de PostgreSQL, incluso entre instancias del servicio. La consulta de disponibilidad se repite después de obtener el bloqueo. Una carrera por crear la misma clave de reserva puede responder 409 al perdedor; debe reintentar la misma solicitud para recuperar el resultado ya comprometido.

### Procesador provisional en Citas

Habilitar `PROCESAMIENTO_LOCAL_HABILITADO=true` en Citas para registrar `POST /interno/solicitudes/{id}/procesar`. Por defecto el controlador no existe. La creación pública de una solicitud **no invoca** este procesador; la solicitud permanece pendiente hasta que un cliente técnico autorizado lo llama.

El procesador bloquea la solicitud, llama a Profesionales y confirma o rechaza según el resultado. Si ya está resuelta, devuelve el estado persistido sin volver a reservar. La conexión HTTP tiene límite de 3 segundos y lectura de 5 segundos. Un error HTTP, timeout o respuesta inconsistente produce 503 y conserva PENDIENTE; no se interpreta como falta de horarios ni se reintenta automáticamente.

Si Profesionales confirma una reserva pero Citas falla antes de confirmar su transacción, el siguiente intento con el mismo ID recupera esa reserva. Esto puede dejar temporalmente una reserva con solicitud pendiente; no existe una transacción distribuida. La recuperación depende del reintento técnico, que debe comprobarse en integración. El adaptador local debe mantenerse restringido a desarrollo; no constituye una implementación de IAM, TLS ni autorización de infraestructura productiva.

## Contrato previsto para notificaciones

RF-19 permanece pendiente. El futuro adaptador deberá consumir un resultado con `solicitudId`, `estudianteId`, `estado` (CONFIRMADA/RECHAZADA) y datos de asignación cuando existan. No debe incluir comentarios emocionales. El destinatario deberá resolverse por un contrato de identidad autorizado, no consultando tablas de Usuarios desde Citas. Usar el ID de solicitud y el resultado como clave de deduplicación.

La notificación se enviará después de persistir la resolución y fuera de su transacción; sus fallos no podrán cambiar el estado ni volver a reservar. En esta versión no existe emisor, consumidor, outbox, registro de envío, proveedor de correo ni reintento de notificaciones. Tampoco hay SQS, Lambda, API Gateway, Dockerfiles, Swarm o pipelines nuevos.

## Límites que requieren revisión posterior

- La bandera `activo` corresponde al perfil en Profesionales. Usuarios no tenía un estado de activación de cuenta: borrar una cuenta o cambiar su rol no sincroniza automáticamente ese perfil. Debe definirse ese contrato de ciclo de vida antes de afirmar que la publicación refleja en tiempo real el estado de la cuenta.
- Diario, Profesionales y Citas validan JWT localmente. Una eliminación de cuenta o cambio de rol no revoca inmediatamente tokens ya emitidos en esos servicios; vencen en una hora. Usuarios sí consulta su base. La revocación distribuida no se implementó.
- Se conserva `ddl-auto=update` para desarrollo. Las restricciones y el SQL real de Hibernate requieren verificación contra PostgreSQL; no se hicieron migraciones ni se iniciaron aplicaciones contra las bases existentes.
- Los listados conservan el estilo sencillo del proyecto sin paginación. Las metas de carga de la matriz no se han medido.
- La finalización exige que el horario haya terminado, además de estado confirmado y propietario, para representar una cita atendida.

## Verificación y siguiente sesión

Resultado de esta sesión: los cuatro servicios finalizaron con `BUILD SUCCESS` usando Java 21. Se ejecutaron ocho pruebas unitarias: dos en Usuarios, dos en Profesionales y cuatro en Citas; todas pasaron, sin errores ni omisiones. Diario se compiló con pruebas omitidas. Esto no verifica consultas JPA, restricciones reales de PostgreSQL, reglas de la cadena HTTP ni concurrencia entre procesos.

Se revisaron los archivos versionados y patrones comunes de claves privadas, tokens y credenciales sin encontrar secretos nuevos. `.env`, paquetes JAR y directorios `target/` siguen fuera de Git. Esta revisión no es una auditoría exhaustiva del historial remoto.

La compilación se realiza desde cada directorio con `.\mvnw.cmd -DskipTests package`. Ese comando compila clases principales y de prueba, pero no ejecuta pruebas ni demuestra funcionamiento integrado.

Se añadieron pruebas unitarias aisladas para la identidad del JWT de Usuarios, la reutilización de resultados de reserva y los efectos de resolución/fallo técnico en Citas. No usan PostgreSQL ni arrancan contextos Spring. Comandos específicos:

```powershell
# Desde ms-usuarios
.\mvnw.cmd -Dtest=JwtFilterTest package
# Desde ms-profesionales
.\mvnw.cmd -Dtest=ReservaServiceTest package
# Desde ms-citas
.\mvnw.cmd -Dtest=ProcesamientoCitaServiceTest package
```

La siguiente sesión de verificación debe cubrir autenticación 401/403, propietario ajeno, registro de profesional solo por administrador, perfiles inactivos, duplicados concurrentes, límites horarios, reservas repetidas y fallos entre servicios. También debe comprobar el diario y el resumen semanal con datos reales. Las pruebas `*ApplicationTests` de contexto siguen pendientes junto con Postman y la integración completa, por el acuerdo de trabajo.
