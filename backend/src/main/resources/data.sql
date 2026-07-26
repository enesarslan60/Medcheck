-- Local-development and unit-test data only.
-- Real drug lookups go through RxNorm (autocomplete) and openFDA
-- (drug_interactions text) at runtime. The drug_interaction_texts cache
-- table is populated on demand by InteractionService, not seeded here.

INSERT INTO drugs (name, active_substance, description, side_effects) VALUES
('Advil', 'Ibuprofen', 'Nonsteroidal anti-inflammatory drug used for pain and fever.', 'Stomach upset, heartburn, dizziness, mild rash.'),
('Aspirin', 'Aspirin', 'Salicylate used for pain relief and as an antiplatelet.', 'GI bleeding, tinnitus, allergic reactions.'),
('Glucophage', 'Metformin', 'First-line oral medication for type 2 diabetes.', 'Nausea, diarrhea, lactic acidosis (rare).'),
('Altace', 'Ramipril', 'ACE inhibitor used for hypertension and heart failure.', 'Dry cough, dizziness, hyperkalemia.'),
('Coumadin', 'Warfarin', 'Anticoagulant used to prevent thrombosis and embolism.', 'Bleeding, bruising, skin necrosis (rare).'),
('Tylenol', 'Paracetamol', 'Analgesic and antipyretic for mild to moderate pain.', 'Liver damage at high doses, rash.'),
('Lipitor', 'Atorvastatin', 'Statin used to lower cholesterol and prevent cardiovascular events.', 'Muscle pain, elevated liver enzymes.'),
('Prilosec', 'Omeprazole', 'Proton pump inhibitor used to reduce stomach acid.', 'Headache, nausea, vitamin B12 deficiency long-term.'),
('Zoloft', 'Sertraline', 'SSRI antidepressant used for depression and anxiety.', 'Insomnia, nausea, sexual dysfunction.'),
('Amoxil', 'Amoxicillin', 'Penicillin antibiotic used for bacterial infections.', 'Rash, diarrhea, allergic reactions.');
