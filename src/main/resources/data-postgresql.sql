INSERT INTO class_flowers (class_name, flower_type) VALUES
('21반', 'sunflower'),
('22반', 'tulip'),
('23반', 'cherry-blossom'),
('24반', 'dandelion'),
('25반', 'lavender'),
('26반', 'cosmos'),
('27반', 'rose'),
('28반', 'hydrangea'),
('29반', 'daisy')
ON CONFLICT (class_name) DO NOTHING;
