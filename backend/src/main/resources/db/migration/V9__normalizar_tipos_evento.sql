UPDATE local_tipos_evento
SET tipo = 'Cumpleaños'
WHERE tipo IN ('Cumpleanos', 'Cumpleaños');

UPDATE local_tipos_evento
SET tipo = 'Quinceaños'
WHERE tipo IN ('Quince anos', 'Quince años', 'Quinceaños');
