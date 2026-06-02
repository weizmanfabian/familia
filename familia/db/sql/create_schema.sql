
CREATE TABLE ciudad (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    departamento VARCHAR(200) NOT NULL
);

CREATE TABLE persona (
    numero_documento VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    correo_electronico VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    ocupacion VARCHAR(20) NOT NULL CHECK (ocupacion IN ('EMPLEADO', 'INDEPENDIENTE', 'PENSIONADO')),
    esViable BOOLEAN,
    ciudad_id INT NOT NULL,
    FOREIGN KEY (ciudad_id) REFERENCES ciudad(id)
);



-- Crear índice para mejorar las búsquedas de autocompletado
CREATE INDEX idx_ciudad_nombre ON ciudad(nombre);
