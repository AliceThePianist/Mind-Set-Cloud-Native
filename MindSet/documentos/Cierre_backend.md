# Revisión técnica del backend original

Revisión del 14 de noviembre de 2026 sobre `features/citas`, iniciada en `d034042` con árbol limpio y rama sincronizada. El alcance local queda revisado y compilado; no equivale a una aceptación integrada de los 19 RF. La parte asíncrona de RF-15 y la notificación RF-19 se mantienen diferidas expresamente.

## Requisitos funcionales

La fuente continúa siendo `Documento_MindSet.docx` y `Requerimientos_MindSet.xlsx`. La guía de ejecución y los contratos están en [Desarrollo_local.md](Desarrollo_local.md).

| RF | Evidencia de implementación | Estado |
| --- | --- | --- |
| 01 | UsuarioService: registro con entidad nueva, correo único normalizado, BCrypt y rol forzado ESTUDIANTE. | Implementado; aceptación HTTP pendiente. |
| 02 | JwtService/JwtFilter: firma, expiración, ID estable y rol; errores de autenticación. | Implementado; interoperabilidad JWT pendiente. |
| 03 | SecurityConfig protege POST /api/usuarios/profesionales con ADMINISTRADOR; el servicio fuerza PROFESIONAL. | Implementado; procedimiento administrativo preparado, no ejecutado. |
| 04 | RegistroDiarioService valida rango 1–5, comentario de hasta 500 y fecha del servidor; unicidad estudiante/fecha. | Implementado; concurrencia PostgreSQL pendiente. |
| 05 | Historial propio y control de propietario; Diario restringido a ESTUDIANTE. | Implementado; prueba unitaria de acceso ajeno aprobada. |
| 06 | Resumen inclusivo de hoy y seis días anteriores, cantidad, promedio y aviso informativo. | Implementado; límites cubiertos unitariamente. |
| 07 | ProfesionalService.crear usa el ID del JWT y exige perfil completo y cuenta vigente. | Implementado; persistencia integrada pendiente. |
| 08 | Actualización mediante mi-perfil, sin aceptar otro propietario; bloqueo de perfil. | Implementado; permisos HTTP pendientes. |
| 09 | Perfiles completos, activos y cuenta existente con rol PROFESIONAL verificada en Usuarios. | Corregido; nuevo contrato pendiente de prueba entre procesos. |
| 10 | HorarioService.listar filtra por profesional del JWT. | Implementado; consulta real pendiente. |
| 11 | Perfil completo/activo, fecha futura y ausencia de superposición bajo bloqueo del profesional. | Implementado; concurrencia real pendiente. |
| 12 | Modificación propia, futura y no reservada; valida nueva intersección. | Implementado; regresiones unitarias aprobadas. |
| 13 | Eliminación propia, futura y no reservada. | Implementado; regresiones unitarias aprobadas. |
| 14 | Fecha o rango preferido; estado inicial PENDIENTE y columna única estudianteActivo. | Implementado; restricción PostgreSQL pendiente. |
| 15 | Reserva transaccional/idempotente y procesamiento REST local optativo. Fallo técnico conserva PENDIENTE. | Núcleo local implementado; SQS/Lambda y entrega automática pendientes por alcance. |
| 16 | Solicitudes propias y denegación de consulta ajena. | Implementado; aceptación HTTP pendiente. |
| 17 | Consulta de citas por profesional asignado. | Implementado; aceptación HTTP pendiente. |
| 18 | Profesional asignado, estado CONFIRMADA y horario terminado para pasar a COMPLETADA. | Implementado; regresiones unitarias aprobadas. |
| 19 | Contrato futuro de resultado documentado; ningún envío de correo. | Reservado para la fase posterior. |

## Reglas de negocio

| Reglas | Revisión |
| --- | --- |
| RN-01 y RN-02 | Unicidad de correo en Usuarios, roles forzados en altas y autorización administrativa. Se evita persistir el ID del cuerpo de registro. |
| RN-03 y RN-04 | Perfil completo antes de publicar disponibilidad; perfil activo y cuenta vigente para publicación y nueva reserva. |
| RN-05 a RN-07 | Unicidad diaria, emoción 1–5 y comentario opcional de hasta 500. El diario no se transmite por contratos internos. |
| RN-08 y RN-09 | Ventana de siete días en America/Santiago, conteo y aviso no clínico. |
| RN-10 a RN-12 | Propietario, fechas futuras, intersección de intervalos y protección de reservados. La fila del profesional serializa escrituras locales de agenda. |
| RN-13 y RN-14 | Una solicitud activa mediante columna única nullable; el cliente no decide estado inicial ni propietario. |
| RN-15 y RN-16 | PENDIENTE resuelve CONFIRMADA o RECHAZADA. Solo una respuesta verificada sin horario produce RECHAZADA; errores de dependencias no lo hacen. |
| RN-17 | PK de reserva por solicitud, unicidad de horario, validación de preferencias y resultado persistido, incluso sin disponibilidad. Repeticiones recuperan el resultado original. |
| RN-18 y RN-19 | Consultas por estudiante propietario o profesional asignado. No existen consultas globales de diarios o citas para administradores. |
| RN-20 | Solo CONFIRMADA pasa a COMPLETADA. Se mantiene la condición ya implementada de horario terminado para representar una atención realizada. |
| RN-21 | No existe aún un emisor de notificaciones. El contrato exige enviarlas después de persistir el resultado y sin modificar el estado ante fallo. |
| RN-22 | Sin endpoints ni estado de cancelación. |

## Correcciones y decisiones

- El alta de Usuarios construye una entidad nueva y copia únicamente correo, contraseña codificada y rol decidido por el servidor. La normalización usa Locale.ROOT.
- Se añadió GET interno de vigencia profesional en Usuarios y un cliente REST en Profesionales. Comprueba existencia y rol sin compartir tablas ni exponer correo o credenciales. No se añadió un sistema de sincronización por eventos.
- Las caídas de Usuarios propagan 503. Si ocurren durante una nueva reserva, la excepción sale de la transacción; no se confirma un resultado de falta de disponibilidad. El rollback efectivo queda pendiente de PostgreSQL.
- Profesional.horarios completa el modelo bidireccional OneToMany/ManyToOne. Horario sigue siendo dueño de la FK; Reserva mantiene OneToOne. No hay cascadas de borrado ni relaciones entre bases.
- La serialización e inyección JSON de la colección de horarios están excluidas con JsonIgnore y comprobadas unitariamente.
- Se preservan el CRUD, las rutas existentes, los estados y el procesador local desactivado por defecto. Citas y Diario no necesitaron cambios en lógica de producción durante esta revisión.
- [Administrador_desarrollo.md](Administrador_desarrollo.md) documenta una promoción controlada, simulación por defecto y confirmación explícita del operador mediante el script SQL. No se crearon administradores ni se ejecutó SQL en esta sesión.

## Compilación y pruebas

Java 21, Spring Boot 4.1.1 y las dependencias existentes. Los cuatro comandos Maven terminan en package, por lo que compilan código y generan el JAR.

| Servicio | Compilación | Pruebas ejecutadas | Resultado |
| --- | --- | --- | --- |
| Usuarios | BUILD SUCCESS | JwtFilterTest (2), UsuarioServiceTest (2), ServicioInternoFilterTest (4) | 8 aprobadas |
| Diario | BUILD SUCCESS | RegistroDiarioServiceTest (2) | 2 aprobadas |
| Profesionales | BUILD SUCCESS | ReservaServiceTest (4), ProfesionalServiceTest (5), HorarioServiceTest (3), ServicioInternoFilterTest (4) | 16 aprobadas |
| Citas | BUILD SUCCESS | ProcesamientoCitaServiceTest (4), SolicitudCitaServiceTest (4), ServicioInternoFilterTest (4) | 12 aprobadas |

Total: **38 pruebas unitarias**, cero fallos, errores u omisiones. Se seleccionaron clases concretas con -Dtest; no se ejecutaron los ApplicationTests de contexto. Las pruebas usan repositorios/clientes simulados y peticiones Servlet en memoria, sin servicios HTTP reales ni PostgreSQL. No prueban una transacción distribuida, la cadena completa de Spring Security ni el DDL de Hibernate.

## Pendiente de verificación integrada

- Registro, login y alta profesional con los tres roles; JWT ausente, malformado y vencido.
- Endpoints internos con clave ausente/incorrecta y usuarios sin permisos técnicos.
- Carga JPA y consultas reales, restricciones únicas y bloqueos con dos peticiones simultáneas.
- Integración Usuarios → Profesionales: cuenta eliminada/no profesional, caída y recuperación.
- Integración Citas → Profesionales: confirmación, rechazo, timeout, repetición y recuperación tras reserva remota confirmada y commit local fallido.
- Script administrativo contra una cuenta de desarrollo elegida por el operador, primero con simulación.
- Persistencia y recuperación tras reinicios, y las pruebas funcionales de Postman acordadas.

## Límites y fase siguiente

La comprobación de vigencia no es revocación global de JWT ni elimina la carrera entre consultar una cuenta y que otro proceso la borre. Los tokens de los servicios que validan localmente siguen vigentes hasta su expiración; las reservas históricas permanecen idempotentes. No se añadió un estado de suspensión de cuentas.

Las consultas públicas ahora dependen de Usuarios para verificar vigencia; si no se puede comprobar, fallan con 503. La configuración de clave interna es necesaria aunque el procesador local de Citas permanezca desactivado.

No se implementaron Dockerfiles, Swarm, CI/CD, SQS, Lambda, API Gateway ni notificaciones provisionales. Tampoco se verificaron objetivos de carga, disponibilidad, TLS, cifrado o métricas de la matriz. No se hizo merge a main, ni se borraron o reconstruyeron bases. La siguiente evaluación solo comenzará cuando el usuario comparta expresamente su planificación.
