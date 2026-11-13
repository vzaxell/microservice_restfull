# Checklist de evidencias para el reporte técnico (PDF)

- [ ] Portada (institución, materia, práctica HU-01, nombre, fecha)
- [ ] Enlace al repositorio Git y captura del historial de commits
- [ ] Swagger UI con los 5 endpoints visibles (`/swagger-ui.html`)
- [ ] Postman/Swagger: POST 201, GET 200, GET por id 200, PUT 200, DELETE 204
- [ ] Postman/Swagger: errores 400 (validación), 404 (no encontrado), 409 (ISBN duplicado)
- [ ] Datos en MongoDB (docker exec / Compass) tras crear un libro
- [ ] Salida de `./mvnw clean verify` con todas las pruebas en verde
- [ ] Reporte JaCoCo (`target/site/jacoco/index.html`) mostrando líneas y ramas >= 80 %
- [ ] Panel de SonarQube: Quality Gate en "Passed", bugs, vulnerabilidades y code smells
- [ ] Conclusiones
