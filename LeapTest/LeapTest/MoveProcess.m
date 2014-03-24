% Process the output data of Leap C++ project.
% LI ZHEN, March 17th, 2014.

for i = 10:13    
    filename = sprintf('FingerMove%d.csv', i);
    % fingerNo isTool x y z time
    mat = csvread(filename);
%     figure;
%     scatter3(mat(:, 3), mat(:, 4), mat(:, 5), '.');
%     title(filename);
    
    fprintf('%d:\n', i);
    [row col] = size(mat);
    time = zeros(row, 1);
    for r = 2:row
        time(r, 1) = mat(r, 6) - mat(r - 1, 6);
        % fprintf('dt:%d\n', time(r, 1));
    end
    fprintf('mean:%d, var:%d\n\n', mean(time(2:row, 1)), var(time(2:row, 1)));
    
end