package com.onion.location;

import com.onion.location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationRestController {

	@Autowired
	private LocationService service;
	
	//지역 이름 및 줄임말 중복
	// Duplicated or OK
	@PostMapping("/locations/check_unique")
	public String checkUnique(@Param("id") Integer id, @Param("name") String name) {
		return service.checkUnique(id, name);
	}
}
