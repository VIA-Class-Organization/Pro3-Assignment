package via.pro3.slaughterhouse.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import via.pro3.slaughterhouse.model.Animal;
import via.pro3.slaughterhouse.repositories.AnimalRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController @RequestMapping("/animal") public class AnimalController
{

  private final AnimalRepository animalRepository;

  public AnimalController(AnimalRepository animalRepository)
  {
    this.animalRepository = animalRepository;
  }

  //  Create new animal
  @PostMapping public ResponseEntity<Animal> createAnimal(
      @RequestBody Animal request)
  {
    try
    {
      Animal saved = animalRepository.save(request);
      // Return 201 Created
      return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    catch (Exception e)
    {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Failed to create animal", e);
    }
  }

  @GetMapping("/{id}") public ResponseEntity<Animal> getAnimal(
      @PathVariable int id)
  {
    return animalRepository.findById(id)
        .map(ResponseEntity::ok)// return okay
        .orElse(ResponseEntity.notFound().build());
  }

  //  Get all by origin (letters only)
  @GetMapping("/origin/{origin}") public List<Animal> getByOrigin(
      @PathVariable String origin)
  {
    return animalRepository.findAll().stream()
        .filter(a -> a.getOrigin().equalsIgnoreCase(origin))
        .collect(Collectors.toList());
  }

  //  Get all by date (query param)
  @GetMapping(params = "date") public List<Animal> getAllAnimalsByDate(
      @RequestParam("date") String dateStr)
  {
    LocalDate date;
    try
    {
      date = LocalDate.parse(dateStr); // yyyy-MM-dd format
    }
    catch (Exception e)
    {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid date format, use yyyy-MM-dd");
    }
    return animalRepository.findAll().stream()
        .filter(a -> a.getArrivalTime().toLocalDate().equals(date))
        .collect(Collectors.toList());
  }

  @GetMapping public List<Animal> getAllAnimals()
  {
    return animalRepository.findAll();
  }
}
