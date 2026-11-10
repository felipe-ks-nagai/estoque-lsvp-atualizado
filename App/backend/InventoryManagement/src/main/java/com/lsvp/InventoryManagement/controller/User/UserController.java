package com.lsvp.InventoryManagement.controller.User;

import org.springframework.security.access.prepost.PreAuthorize;

import com.lsvp.InventoryManagement.dto.User.UserUpdateDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//Estava importando o RequestBody errado ); Eu odeio minha vida

import com.lsvp.InventoryManagement.service.UserService;
import com.lsvp.InventoryManagement.dto.User.UserCreateDTO;
import com.lsvp.InventoryManagement.dto.User.UserDTO;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
// Lucas: Alterei o final da rota de users para user
// para usá-la como padrão em tudo que seja relativo ao usuário
//Gustavo: Faz sentido 😆
@Tag(name = "Usuários", description = "Gerenciamento de usuários")

@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO dto){
        System.out.println(dto);
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto){

        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        // if(!userService.existsById(id)){
        //     return ResponseEntity.notFound().build();
        // }

        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Admin busca qualquer um; os demais só podem buscar a si mesmos
    @PreAuthorize("hasRole('ADMINISTRADOR') or #name == authentication.name")
    @GetMapping("/n/{name}")
    public ResponseEntity<UserDTO> getUserByName(@PathVariable String name){
        return ResponseEntity.ok(userService.getUserByName(name));
    }


    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String name
    ){
        Page<UserDTO> result = userService.getUsersPaged(page, limit, name);
        return ResponseEntity.ok(result);
    }

}
