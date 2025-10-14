import resources.Empleado;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        List<Empleado> empleados = new ArrayList<>();
        loadEmpleados(empleados);

        //To-do: Filtrar empleados por un atributo: departamento
        Predicate<Empleado> itDept = e -> "Informática".equalsIgnoreCase(e.getDepartamento());

        //To-do: Ordenar empleados por un atributo: Nombre
        Comparator<Empleado> porNombre = Comparator.comparing(Empleado::getNombre);

        //To-do: Generar un mapa que me permita tener como clave los departamentos y como valor el total de empleados por departamento
        Function<List<Empleado>, Map<String, Integer>> deptCount = list -> {
            Map<String, Integer> map = new HashMap<>();
            for (Empleado e : list) {
                map.merge(e.getDepartamento(), 1, Integer::sum);
            }
            return map;
        };
        //Con stream
        Function<List<Empleado>, Map<String, Integer>> deptCountS = list ->
                list.stream()
                        .collect(Collectors.groupingBy(
                                Empleado::getDepartamento,
                                Collectors.collectingAndThen(
                                        Collectors.counting(),
                                        Long::intValue
                                )
                        ));


        //To-do: Mostrar empleados por un consumer: Contratados en determinado mes
        Consumer<Empleado> showIfJanuary = e -> {
            if (e.getFechaIng().getMonth() == Month.JANUARY) {
                System.out.println(e);
            }
        };

        //Con stream
        Consumer<Empleado> show = System.out::println;


        //Uso de las funciones
        //1. Predicate - Sin stream
        System.out.println("Predicate result");
        List<Empleado> itEmployees = new ArrayList<>();
        for (Empleado e : empleados) {
            if (itDept.test(e)) {
                itEmployees.add(e);
            }
        }
        System.out.println(itEmployees);
        //1. Predicate - Con stream
        List<Empleado> itEmpleados = empleados.stream()
                .filter(itDept)
                .toList();
        System.out.println(itEmpleados);


        //2. Comparator - Sin stream
        System.out.println("Comparator result");
        List<Empleado> orderEmpleados = new ArrayList<>(List.copyOf(empleados));
        orderEmpleados.sort(porNombre);
        System.out.println(orderEmpleados);
        //2. Comparator - Con stream
        List<Empleado> sorted = empleados.stream()
                .sorted(porNombre)
                .toList();
        System.out.println(sorted);
        //sorted.forEach(System.out::println);

        //3. Function - Sin stream
        System.out.println("Function result");
        Map<String, Integer> totalPorDept = deptCount.apply(empleados);
        //3. Function - Con stream
        Map<String, Integer> totalPorDeptS = deptCountS.apply(empleados);
        System.out.println(totalPorDept);

        //4. Consumer - Sin stream
        System.out.println("Consumer result");
        for (Empleado e : empleados) {
            showIfJanuary.accept(e);
        }
        //4. Consumer - Con stream
        empleados.stream()
                .filter(e -> e.getFechaIng().getMonth() == Month.JANUARY)
                .forEach(show);

        //To-do: Solucion taller 3: stream y lambdas

        /* 1. Calcule estadisticas de salario (minimo, maximo, promedio)
         */
        System.out.println("Stream y lambdas result: Calcule estadisticas de salario");
        BigDecimal salarioMinimo = empleados.stream()
                .map((Empleado::getSalario))
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        System.out.println("Salario minimo: " + salarioMinimo);

        System.out.println("Stream y lambdas result: ");
        BigDecimal salarioMaximo = empleados.stream()
                .map((Empleado::getSalario))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        System.out.println("Salario maximo: " + salarioMaximo);

        System.out.println("Stream y lambdas result: ");
        Double salarioPromedio = empleados.stream()
                .collect(Collectors.averagingDouble(listEmpleados ->
                        listEmpleados.getSalario().doubleValue()));
        System.out.println("Salario promedio: " + salarioPromedio);

        /* 2. Agrupe por genero y departamento
         */
        System.out.println("Stream y lambdas result: Agrupe por genero y departamento");
        Map<String, Map<String, List<Empleado>>> generoPorDepartamentos = empleados.stream()
                .collect(Collectors.groupingBy(Empleado::getGenero,
                        Collectors.groupingBy(Empleado::getDepartamento)));

        generoPorDepartamentos.forEach((genero, departamento) -> {
            System.out.println("Genero: " + genero);
            departamento.forEach((listDepartamentos, listEmpleados) -> {
                System.out.println("Departamento: " + listDepartamentos);
                listEmpleados.forEach(System.out::println);
            });
        });

        /* 3. Obtenga el top 3 de empleados con mas antiguedad
         */
        System.out.println("Stream y lambdas result: top 3 de empleados con mas antiguedad");
        List<Empleado> listaTopEmpleadosAntiguos = empleados.stream()
                .filter(empleado -> empleado.getActive().equals(Boolean.TRUE))
                .sorted(Comparator.comparing(Empleado::getFechaIng))
                .limit(3)
                .toList();
        listaTopEmpleadosAntiguos.forEach(System.out::println);

        /* 4. Genere un mapa Map<String, Double> con el porcentaje de empleados
        activos por departamento
         */
        System.out.println("Stream y lambdas result: porcentaje empleados por departamento");
        Map<String, Double> porcentajeEmpleadosActivosPorDepartamento = empleados.stream()
                .collect(Collectors.groupingBy(Empleado::getDepartamento, Collectors.teeing(
                        Collectors.filtering(Empleado::getActive, Collectors.counting()),
                        Collectors.counting(),
                        (activosPorDept, totalPorDepartamento) -> (totalPorDepartamento == 0)
                                ? 0.0 : (activosPorDept * 100) / totalPorDepartamento)
                ));
        porcentajeEmpleadosActivosPorDepartamento.forEach((departamento, porcentaje) ->
                System.out.println("Departamento: " + departamento + " porcentaje empleados activos: " + porcentaje));

        /* 5. Compare el rendimiento entre .stream() y .parallelStream()
        (tiempo de ejecucion) genereando una lista de 5 millones de empleados con
        datos aleatorios. Calcular:
         */
        List<String> nombres = List.of("Camila", "Camilo", "Carlos", "Estefanía", "Juan", "José", "Julián", "María", "Raúl");
        List<String> apellidos = List.of("Flores", "Franco", "Gutierrez", "López", "Rodríguez", "Silva");
        List<String> generos = List.of("F", "M");
        List<String> cargos = List.of("Asistente Contable", "Desarrollador", "Tester QA", "Reclutador", "Soporte TI");
        List<String> departamento = List.of("Contabilidad", "Informática", "Talento Humano");

        System.out.println("Stream y lambdas result: Compare el rendimiento entre .stream() y .parallelStream()");
        List<Empleado> empleadosGenerados = IntStream.range(0, 5_000_000)
                .mapToObj(item -> new Empleado(
                        nombres.get((int) (Math.random() * nombres.size())),
                        apellidos.get((int) (Math.random() * apellidos.size())),
                        generos.get((int) (Math.random() * generos.size())),
                        departamento.get((int) (Math.random() * departamento.size())),
                        cargos.get((int) (Math.random() * cargos.size())),
                        BigDecimal.valueOf((int) (Math.random() * 1000) + 300),
                        LocalDate.now().minusDays((int) (Math.random() * 3650))
                ))
                .toList();

        /* a. Salario promedio (usando stream() y parallelStream()
         */
        System.out.println("Stream y lambdas result: Salario promedio");
        Instant inicioSecuencial = Instant.now();
        Double promedioSecuencial = empleadosGenerados.stream()
                .mapToDouble(empleado -> empleado.getSalario().doubleValue())
                .average().orElse(0.0);
        Instant finalSecuencial = Instant.now();
        System.out.println("PROMEDIO SECUENCIAL:");
        System.out.println("Resultado promedio: " + promedioSecuencial + ", tiempo: " +
                Duration.between(inicioSecuencial, finalSecuencial).abs().toMillis() + "ms");

        System.out.println("PROMEDIO PARALELO:");
        Instant inicioParalelo = Instant.now();
        Double promedioParalelo = empleadosGenerados.parallelStream()
                .mapToDouble(empleado -> empleado.getSalario().doubleValue())
                .average().orElse(0.0);
        Instant finalParalelo = Instant.now();
        System.out.println("Resultado promedio: " + promedioSecuencial + ", tiempo: " +
                Duration.between(inicioParalelo, finalParalelo).abs().toMillis() + "ms");

        /* b. Cantidad de empleados por departamento (Collectors.groupingBy)
         */
        System.out.println("Stream y lambdas result: Cantidad de empleados por departamento");
        Instant inicioCantidadEmpleadosSecuencial = Instant.now();
        Map<String, Long> cantidadEmpleadosPorDepartamento = empleadosGenerados.stream()
                .collect(Collectors.groupingBy(Empleado::getDepartamento,
                        Collectors.counting()));
        Instant finalCantidadEmpleadosSecuencial = Instant.now();
        System.out.println("Cantidad empleados por departamento secuencial: " + cantidadEmpleadosPorDepartamento +
                ", tiempo: " + Duration.between(inicioCantidadEmpleadosSecuencial, finalCantidadEmpleadosSecuencial).toMillis() + "ms");

        Instant inicioCantidadEmpleadosParalelo = Instant.now();
        Map<String, Long> cantidadEmpleadosPorDepartamento2 = empleadosGenerados.parallelStream()
                .collect(Collectors.groupingBy(Empleado::getDepartamento,
                        Collectors.counting()));
        Instant finalCantidadEmpleadosParalelo = Instant.now();
        System.out.println("Cantidad empleados por departamento paralelo: " + cantidadEmpleadosPorDepartamento2 +
                ", tiempo: " + Duration.between(inicioCantidadEmpleadosParalelo, finalCantidadEmpleadosParalelo).toMillis() + "ms");
    }

    public static void loadEmpleados(List<Empleado> empleadoList) {
        empleadoList.add(new Empleado("María", "Rodríguez", "F", "Contabilidad", "Asistente Contable", new BigDecimal(700), LocalDate.parse("2021-04-01")));
        empleadoList.add(new Empleado("Juan", "Gutierrez", "M", "Talento Humano", "Reclutador", new BigDecimal(500), LocalDate.parse("2023-03-11"), LocalDate.parse("2024-04-01"), false));
        empleadoList.add(new Empleado("José", "Albornoz", "M", "Contabilidad", "Asistente Contable", new BigDecimal(800), LocalDate.parse("2020-08-15"), LocalDate.parse("2023-05-01"), false));
        empleadoList.add(new Empleado("Julián", "Flores", "M", "Informática", "Soporte TI", new BigDecimal(800), LocalDate.parse("2023-11-01")));
        empleadoList.add(new Empleado("Camila", "Mendoza", "F", "Informática", "Desarrollador UI/UX", new BigDecimal(1000), LocalDate.parse("2021-07-08")));
        empleadoList.add(new Empleado("Camilo", "López", "M", "Contabilidad", "Supervisor Contable", new BigDecimal(1500), LocalDate.parse("2020-04-11")));
        empleadoList.add(new Empleado("Manuel", "Játiva", "M", "Contabilidad", "Asistente Contable", new BigDecimal(850), LocalDate.parse("2023-06-03")));
        empleadoList.add(new Empleado("Carlos", "Franco", "M", "Talento Humano", "Reclutador", new BigDecimal(650), LocalDate.parse("2023-01-07"), LocalDate.parse("2024-12-09"), false));
        empleadoList.add(new Empleado("Raúl", "Echeverría", "M", "Informática", "Infraestructura TI", new BigDecimal(950), LocalDate.parse("2020-02-14")));
        empleadoList.add(new Empleado("Estefanía", "Mendoza", "F", "Talento Humano", "Supervisora TH", new BigDecimal(1600), LocalDate.parse("2021-09-21")));
        empleadoList.add(new Empleado("Julie", "Flores", "F", "Informática", "Desarrollador", new BigDecimal(1200), LocalDate.parse("2021-12-10")));
        empleadoList.add(new Empleado("Melissa", "Morocho", "F", "Contabilidad", "Asistente Contable", new BigDecimal(820), LocalDate.parse("2022-05-22"), LocalDate.parse("2023-07-09"), false));
        empleadoList.add(new Empleado("Camila", "Mendez", "F", "Contabilidad", "Asistente Cuentas", new BigDecimal(860), LocalDate.parse("2020-10-01")));
        empleadoList.add(new Empleado("José", "Rodríguez", "M", "Informática", "Tester QA", new BigDecimal(1100), LocalDate.parse("2021-10-01")));
        empleadoList.add(new Empleado("Esteban", "Gutierrez", "M", "Talento Humano", "Reclutador", new BigDecimal(700), LocalDate.parse("2023-04-01")));
        empleadoList.add(new Empleado("María", "López", "F", "Contabilidad", "Asistente Contable", new BigDecimal(840), LocalDate.parse("2020-02-20"), LocalDate.parse("2024-07-15"), false));
        empleadoList.add(new Empleado("Cecilia", "Marín", "F", "Informática", "Supervisora TI", new BigDecimal(2000), LocalDate.parse("2020-04-21")));
        empleadoList.add(new Empleado("Edison", "Cáceres", "M", "Informática", "Desarrollador TI", new BigDecimal(1300), LocalDate.parse("2023-07-07")));
        empleadoList.add(new Empleado("María", "Silva", "F", "Contabilidad", "Asistente Contable", new BigDecimal(900), LocalDate.parse("2021-11-15"), LocalDate.parse("2022-08-09"), false));
    }
}