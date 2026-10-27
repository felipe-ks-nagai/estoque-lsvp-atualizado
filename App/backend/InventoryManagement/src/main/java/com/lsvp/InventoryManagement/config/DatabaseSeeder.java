package com.lsvp.InventoryManagement.config;

import com.lsvp.InventoryManagement.entity.User; 
import com.lsvp.InventoryManagement.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Configuration;
import com.lsvp.InventoryManagement.enums.Role;

@Configuration // Indica que é uma classe de configuração do Spring
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existe um usuário admin para não duplicar toda vez que subir a aplicação
        if (userRepository.findByName("admin").isEmpty()) {
            
            // Crie a instância do usuário conforme o seu Model/Entity
            User admin = new User();
            admin.setName("admin");
            // Criptografa a senha usando exatamente o mesmo PasswordConfig da aplicação
            admin.setPassword(passwordEncoder.encode("admin")); 
            admin.setRole(Role.ADMINISTRADOR); // Ajuste conforme o seu Enum ou String de Role

            userRepository.save(admin);
            
            System.out.println(">>> SEED: Usuário 'admin' criado com sucesso!");
        } else {
            System.out.println(">>> SEED: Usuário 'admin' já existe no banco.");
        }
    }
}
