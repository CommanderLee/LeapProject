% Process the output data of Leap C++ project.
% LI ZHEN, March 17th, 2014.

for i = 8:8
    
    filename = sprintf('FingerMove%d.csv', i);
    % fingerNo isTool x y z
    mat = csvread(filename);
    figure;
    scatter3(mat(:, 3), mat(:, 4), mat(:, 5), '.');
    title(filename);
end